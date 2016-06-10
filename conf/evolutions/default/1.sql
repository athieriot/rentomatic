# Invoices schema

# --- !Ups

CREATE TABLE invoice (
    id UUID NOT NULL,
    movie_id BIGINT NOT NULL,
    date TIMESTAMP NOT NULL,
    paid FLOAT NOT NULL,
    return_date TIMESTAMP,
    extra_charge FLOAT,
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE invoice;