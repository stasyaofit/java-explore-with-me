CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    email VARCHAR(254)                            NOT NULL,
    name  VARCHAR(250)                            NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT check_user_email_len CHECK (LENGTH(email) >= 6 AND LENGTH(email) <= 254),
    CONSTRAINT check_user_name_len CHECK (LENGTH(name) >= 2 AND LENGTH(name) <= 250)
);

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(250)                            NOT NULL,
    CONSTRAINT pk_cat PRIMARY KEY (id),
    CONSTRAINT uq_cat_name UNIQUE (name),
    CONSTRAINT check_cat_name_len CHECK (LENGTH(name) >= 1 AND LENGTH(name) <= 50)
);

CREATE TABLE IF NOT EXISTS locations
(
    id  BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    lat FLOAT,
    lon FLOAT,
    CONSTRAINT pk_location PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation         VARCHAR(2000)                           NOT NULL,
    category_id        BIGINT REFERENCES categories (id)       NOT NULL,
    created_on         TIMESTAMP WITHOUT TIME ZONE             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description        VARCHAR(7000)                           NOT NULL,
    event_date         TIMESTAMP                               NOT NULL,
    initiator_id       BIGINT REFERENCES users (id)            NOT NULL,
    location_id        BIGINT REFERENCES locations (id)        NOT NULL,
    paid               BOOLEAN                                 NOT NULL,
    participant_limit  INTEGER                                 NOT NULL,
    published_on       TIMESTAMP,
    request_moderation BOOLEAN                                 NOT NULL,
    state              VARCHAR(255)                            NOT NULL,
    title              VARCHAR(120)                            NOT NULL,
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT check_event_annotation_len CHECK (LENGTH(annotation) >= 20 AND LENGTH(annotation) <= 2000),
    CONSTRAINT check_event_description_len CHECK (LENGTH(description) >= 20 AND LENGTH(description) <= 7000),
    CONSTRAINT check_event_title_len CHECK (LENGTH(title) >= 3 AND LENGTH(title) <= 120)
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created      TIMESTAMP WITHOUT TIME ZONE             NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_id     BIGINT REFERENCES events (id)           NOT NULL,
    requester_id BIGINT REFERENCES users (id)            NOT NULL,
    status       VARCHAR(255)                            NOT NULL,
    CONSTRAINT pk_requests PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    pinned BOOLEAN                                 NOT NULL,
    title  VARCHAR(120)                            NOT NULL,
    CONSTRAINT pl_compilations PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS compilation_events
(
    compilation_id BIGINT REFERENCES compilations (id) ON DELETE CASCADE,
    event_id       BIGINT REFERENCES events (id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    text         VARCHAR(800),
    created_on TIMESTAMP WITHOUT TIME ZONE,
    user_id      BIGINT,
    event_id     BIGINT,
    CONSTRAINT fk_comment_to_events
        FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_comment_to_users
        FOREIGN KEY (user_id) REFERENCES users (id)
);

create index categories_id_index
    on categories (id);

create index compilation_events_compilation_id_index
    on compilation_events (compilation_id);

create index compilations_id_index
    on compilations (id);

create index events_id_category_id_index
    on events (id, category_id);

create index locations_id_index
    on locations (id);

create index requests_id_event_id_index
    on requests (id, event_id);

create index comments_id_event_id_index
    on comments (event_id);