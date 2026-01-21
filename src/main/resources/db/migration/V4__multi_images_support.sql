CREATE TABLE album_images (
    album_id UUID NOT NULL,
    image_key VARCHAR(255) NOT NULL,
    FOREIGN KEY (album_id) REFERENCES albums(id) ON DELETE CASCADE
);

INSERT INTO album_images (album_id, image_key)
SELECT id, cover_url FROM albums WHERE cover_url IS NOT NULL;

ALTER TABLE albums DROP COLUMN cover_url;
