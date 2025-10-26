package com.evtrading.swp391.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocuSealService {

    private static final Logger log = LoggerFactory.getLogger(DocuSealService.class);

    @Value("${docuseal.api.url:https://api.docuseal.com}")
    private String apiBaseUrl;

    @Value("${docuseal.api.key:}")
    private String apiKey;

    // Default to submissions API; can be overridden to /api/envelopes if needed
    @Value("${docuseal.api.createEnvelopePath:/submissions}")
    private String createPath;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public static class CreateResult {
        public final String envelopeId; // submission/envelope id
        public final String signingUrl; // link for signer to open
        public final Map<String, Object> raw;

        public CreateResult(String envelopeId, String signingUrl, Map<String, Object> raw) {
            this.envelopeId = envelopeId;
            this.signingUrl = signingUrl;
            this.raw = raw;
        }
    }

    public CreateResult createEnvelope(String templateId, String signerEmail, String signerName, String content) {
        if (apiKey == null || apiKey.isBlank() || apiBaseUrl == null || apiBaseUrl.isBlank()) {
            throw new IllegalStateException("DocuSeal API credentials are not configured");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        // Submissions-style
        payload.put("template_id", tryParseInt(templateId));
        payload.put("send_email", true);
        Map<String, Object> variables = new HashMap<>();
        if (content != null && !content.isBlank()) {
            variables.put("content", content);
        }
        if (!variables.isEmpty()) {
            payload.put("variables", variables);
        }
        List<Map<String, Object>> submitters = new ArrayList<>();
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("role", "First Party");
        s.put("email", signerEmail);
        if (signerName != null && !signerName.isBlank()) s.put("name", signerName);
        submitters.add(s);
        payload.put("submitters", submitters);

        // Also include aliases in case tenant expects envelopes-style
        payload.putIfAbsent("templateId", templateId);
        Map<String, Object> signer = new LinkedHashMap<>();
        signer.put("email", signerEmail);
        signer.put("name", signerName);
        payload.put("signer", signer);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
    headers.set("X-Auth-Token", apiKey);

        String url = normalizeUrl(apiBaseUrl, createPath);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        try {
            return doCreate(url, entity);
        } catch (HttpStatusCodeException e) {
            // Fallback strategy: if configured path 404s, try the other common path
            if (e.getStatusCode().value() == 404) {
                String fallback = "/submissions".equalsIgnoreCase(createPath) ? "/api/envelopes" : "/submissions";
                try {
                    return doCreate(normalizeUrl(apiBaseUrl, fallback), entity);
                } catch (HttpStatusCodeException e2) {
                    throw new RuntimeException("DocuSeal create error (fallback): " + e2.getStatusCode() + " " + e2.getResponseBodyAsString(), e2);
                } catch (Exception e2) {
                    throw new RuntimeException("DocuSeal create error (fallback): " + e2.getMessage(), e2);
                }
            }
            throw new RuntimeException("DocuSeal create error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("DocuSeal create error: " + e.getMessage(), e);
        }
    }

    private CreateResult doCreate(String url, HttpEntity<Map<String, Object>> entity) throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(URI.create(url), HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("DocuSeal create failed: " + response.getStatusCode());
        }

        Map<String, Object> body = Collections.emptyMap();
        String raw = response.getBody();
        if (log.isDebugEnabled()) {
            log.debug("DocuSeal create response: status={} headers={} body={}", response.getStatusCode(), response.getHeaders(), raw);
        }
        try {
            if (raw != null && !raw.isBlank()) {
                body = mapper.readValue(raw, new TypeReference<>() {});
            }
        } catch (Exception parseEx) {
            // tolerate non-JSON response and continue
            if (log.isDebugEnabled()) log.debug("DocuSeal response body not JSON: {}", parseEx.getMessage());
        }

        String id = findId(body);
        if (id == null) {
            // Try Location header: .../submissions/{id} or .../envelopes/{id}
            String loc = Optional.ofNullable(response.getHeaders().getFirst("Location")).orElse(null);
            if (loc != null) {
                int slash = loc.lastIndexOf('/') + 1;
                if (slash > 0 && slash < loc.length()) {
                    id = loc.substring(slash);
                }
            }
            if (id == null) {
                // Try other potential headers just in case
                String hdr = Optional.ofNullable(response.getHeaders().getFirst("X-Submission-Id")).orElse(null);
                if (hdr == null) hdr = response.getHeaders().getFirst("X-Envelope-Id");
                if (hdr != null && !hdr.isBlank()) id = hdr;
            }
        }

        String signingUrl = findSigningUrl(body);
        return new CreateResult(id, signingUrl, body);
    }

    private static String normalizeUrl(String base, String path) {
        String b = base.endsWith("/") ? base.substring(0, base.length()-1) : base;
        String p = path.startsWith("/") ? path : "/" + path;
        return b + p;
    }

    private static Integer tryParseInt(String v) {
        try { return v == null ? null : Integer.parseInt(v); } catch (NumberFormatException e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private String findId(Map<String, Object> body) {
        Object id = body.get("id");
        if (id == null) id = body.get("envelope_id");
        if (id == null) id = body.get("submission_id");
        if (id == null) {
            // Sometimes APIs wrap under a named object
            Object sub = body.get("submission");
            if (sub instanceof Map<?,?> m) {
                Object v = ((Map<String, Object>) m).get("id");
                if (v == null) v = ((Map<String, Object>) m).get("submission_id");
                if (v != null) id = v;
            }
            Object env = body.get("envelope");
            if (id == null && env instanceof Map<?,?> m2) {
                Object v2 = ((Map<String, Object>) m2).get("id");
                if (v2 == null) v2 = ((Map<String, Object>) m2).get("envelope_id");
                if (v2 != null) id = v2;
            }
        }
        if (id == null) {
            // sometimes nested
            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data != null) {
                id = data.get("id");
                if (id == null) id = data.get("submission_id");
                if (id == null) id = data.get("envelope_id");
                if (id == null) {
                    Object sub = data.get("submission");
                    if (sub instanceof Map<?,?> m) {
                        Object v = ((Map<String, Object>) m).get("id");
                        if (v == null) v = ((Map<String, Object>) m).get("submission_id");
                        if (v != null) id = v;
                    }
                    Object env = data.get("envelope");
                    if (id == null && env instanceof Map<?,?> m2) {
                        Object v2 = ((Map<String, Object>) m2).get("id");
                        if (v2 == null) v2 = ((Map<String, Object>) m2).get("envelope_id");
                        if (v2 != null) id = v2;
                    }
                }
            }
        }
        return id == null ? null : String.valueOf(id);
    }

    @SuppressWarnings("unchecked")
    private String findSigningUrl(Map<String, Object> body) {
        // common locations
        String[] keys = {"signing_url", "sign_url", "url", "link"};
        for (String k : keys) {
            Object v = body.get(k);
            if (v instanceof String s && !s.isBlank()) return s;
        }
        // check submitters array
        Object submitters = body.get("submitters");
        if (submitters instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> m) {
                for (String k : keys) {
                    Object v = ((Map<String, Object>) m).get(k);
                    if (v instanceof String s && !s.isBlank()) return s;
                }
            }
        }
        // nested data
        Object data = body.get("data");
        if (data instanceof Map<?,?> m) {
            for (String k : keys) {
                Object v = ((Map<String, Object>) m).get(k);
                if (v instanceof String s && !s.isBlank()) return s;
            }
        }
        return null;
    }
}
