package no.voiestad.f1.response;

public class ReferralCodeResponse {
    public final String code;

    public ReferralCodeResponse(Long code) {
        if (code != null) {
            this.code = String.valueOf(code);
        } else {
            this.code = null;
        }
    }
}
