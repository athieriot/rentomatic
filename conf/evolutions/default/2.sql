# Invoices schema

# --- !Ups

ALTER TABLE invoice ADD COLUMN release_type VARCHAR(30) NOT NULL;

# --- !Downs

ALTER TABLE invoice DROP COLUMN release_type;