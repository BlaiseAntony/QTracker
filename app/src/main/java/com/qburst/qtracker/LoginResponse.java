package com.qburst.qtracker;

class LoginResponse {
    private int status;
    private PayLoad payload;

    public int getStatus() {
        return status;
    }

    public PayLoad getPayload() {
        return payload;
    }

    class PayLoad {
        private AccessToken accessToken;

        public AccessToken getAccessToken() {
            return accessToken;
        }
    }

    public class AccessToken {
        private String token;
        private String issuedAt;

        public String getToken() {
            return token;
        }

        public String getIssuedAt() {
            return issuedAt;
        }
    }
}