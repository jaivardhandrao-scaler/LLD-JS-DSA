package com.assignment.question;

@WithBuilder
public class DatabaseConfigurationBuilder {
    
    private String databaseUrl;
    private String username;
    private String password;
    private int maxConnections;
    private boolean enableCache;
    private boolean isReadOnly;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private DatabaseConfigurationBuilder databaseConfigurationBuilder;

        public Builder() {
            databaseConfigurationBuilder = new DatabaseConfigurationBuilder();
        }


        public Builder withDatabaseUrl(String databaseUrl) {
            databaseConfigurationBuilder.databaseUrl = databaseUrl;
            return this;
        }

        public Builder withCredentials(String username, String password) {
            databaseConfigurationBuilder.username = username;
            databaseConfigurationBuilder.password = password;
            return this;
        }

        public Builder withMaxConnections(int maxConnections) {
            databaseConfigurationBuilder.maxConnections = maxConnections;
            return this;
        }

        public Builder withEnableCache(boolean enableCache) {
            databaseConfigurationBuilder.enableCache = enableCache;
            return this;
        }

        public Builder withReadOnly(boolean isReadOnly) {
            databaseConfigurationBuilder.isReadOnly = isReadOnly;
            return this;
        }

        public DatabaseConfigurationBuilder build() {
            DatabaseConfigurationBuilder databaseConfigurationBuilder = new DatabaseConfigurationBuilder();
            databaseConfigurationBuilder.databaseUrl = this.databaseConfigurationBuilder.databaseUrl;
            databaseConfigurationBuilder.username = this.databaseConfigurationBuilder.username;
            databaseConfigurationBuilder.password = this.databaseConfigurationBuilder.password;
            databaseConfigurationBuilder.maxConnections = this.databaseConfigurationBuilder.maxConnections;
            databaseConfigurationBuilder.enableCache = this.databaseConfigurationBuilder.enableCache;
            databaseConfigurationBuilder.isReadOnly = this.databaseConfigurationBuilder.isReadOnly;
            return databaseConfigurationBuilder;
        }
        
    }
}