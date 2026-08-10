CREATE SEQUENCE users_seq INCREMENT BY 50;

CREATE TABLE users
(
    id BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

ALTER SEQUENCE users_seq OWNED BY users.id;



CREATE SEQUENCE news_seq INCREMENT BY 50;

CREATE TABLE news
(
    id BIGINT PRIMARY KEY DEFAULT nextval('news_seq'),
    user_id BIGINT NOT NULL,
    title VARCHAR(255),
    content TEXT NOT NULL,
    date_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_news_user FOREIGN KEY (user_id) REFERENCES users (id)
);

ALTER SEQUENCE news_seq  OWNED BY news.id;

