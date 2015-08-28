# --- !Ups

-- Insert user's information into USER.
INSERT INTO "USER" (NAME, PASSWORD, FULL_NAME, EMAIL, IS_ADMIN, AGE) VALUES ('scala', 'password', 'Martin Odersky', 'scala@gmail.com', true, 45);
INSERT INTO USER (NAME, PASSWORD, FULL_NAME, EMAIL, IS_ADMIN, AGE) VALUES ('play2', 'password', 'play framework', 'playframework@gmail.com', false, 20);
INSERT INTO USER (NAME, PASSWORD, FULL_NAME, EMAIL, IS_ADMIN, AGE) VALUES ('spark', 'password', 'Spark Databricks', 'spark@gmail.com', false, 20);

-- Insert post's information into POST.
INSERT INTO POST (TITLE, CONTENT, POSTED_AT, AUTHOR_ID) VALUES ('Functional programming in scala', 'content1...', '2015-01-01', 1);
INSERT INTO POST (TITLE, CONTENT, POSTED_AT, AUTHOR_ID) VALUES ('play2 framework cookbook', 'content2...', '2015-01-01', 2);
INSERT INTO POST (TITLE, CONTENT, POSTED_AT, AUTHOR_ID) VALUES ('Learning Spark', 'content3...', '2015-01-01', 3);


# --- !Downs

DELETE FROM POST;

DELETE FROM USER;

