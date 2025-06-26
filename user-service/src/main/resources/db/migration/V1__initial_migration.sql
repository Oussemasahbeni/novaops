CREATE TABLE user_roles
(
    id      UUID         NOT NULL,
    user_id UUID         NOT NULL,
    name    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (id)
);

CREATE TABLE users
(
    id               UUID         NOT NULL,
    created_by       VARCHAR(255),
    last_modified_by VARCHAR(255),
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    version          SMALLINT     NOT NULL,
    first_name        VARCHAR(255) NOT NULL,
    last_name        VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    phone_number     VARCHAR(255),
    profile_picture  VARCHAR(255),
    locale           VARCHAR(255),
    birth_date       date,
    address          VARCHAR(255),
    gender           VARCHAR(255),
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE user_roles
    ADD CONSTRAINT FK_USER_ROLES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);