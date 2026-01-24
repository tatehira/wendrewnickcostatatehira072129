ALTER TABLE artists ADD COLUMN is_band BOOLEAN NOT NULL DEFAULT true;

UPDATE artists SET is_band = false WHERE id IN (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03'
);

UPDATE artists SET is_band = true WHERE id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04';
