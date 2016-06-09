# Invoices schema

# --- !Ups

CREATE TABLE invoice (
    id UUID NOT NULL,
    movie_id BIGINT NOT NULL,
    date TIMESTAMP NOT NULL,
    paid DOUBLE NOT NULL,
    return_date TIMESTAMP,
    extra_charge DOUBLE,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE invoice;