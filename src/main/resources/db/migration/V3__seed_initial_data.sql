-- Migration V3: Seed Initial Data
-- Author: Music Manager Team
-- Description: Inserts required initial data for Artists and Albums

INSERT INTO artists (id, name) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'Serj Tankian');

INSERT INTO albums (id, title) VALUES 
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'Harakiri'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'Black Blooms'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'The Rough Dog');

INSERT INTO album_artists (artist_id, album_id) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b01'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b02'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b03');


INSERT INTO artists (id, name) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'Mike Shinoda');

INSERT INTO albums (id, title) VALUES 
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'The Rising Tied'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'Post Traumatic'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b06', 'Post Traumatic EP'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b07', 'Where’d You Go');

INSERT INTO album_artists (artist_id, album_id) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b04'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b05'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b06'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b07');


INSERT INTO artists (id, name) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'Michel Teló');

INSERT INTO albums (id, title) VALUES 
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b08', 'Bem Sertanejo'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b09', 'Bem Sertanejo - O Show (Ao Vivo)'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b10', 'Bem Sertanejo - (1ª Temporada) - EP');

INSERT INTO album_artists (artist_id, album_id) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b08'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b09'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b10');


INSERT INTO artists (id, name) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'Guns N’ Roses');

INSERT INTO albums (id, title) VALUES 
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'Use Your Illusion I'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12', 'Use Your Illusion II'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13', 'Greatest Hits');

INSERT INTO album_artists (artist_id, album_id) VALUES 
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b12'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b13');
