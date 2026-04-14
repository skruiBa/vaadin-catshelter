INSERT INTO tags (id, name, category, description, color_code, icon) VALUES
  (1, 'Ystävällinen', 'Luonne', 'Nauttii ihmisten seurasta', '#4CAF50', 'smile'),
  (2, 'Leikkisä', 'Luonne', 'Tykkää leluista ja aktivoinnista', '#03A9F4', 'gamepad'),
  (3, 'Pelkää lapsia', 'Huomio', 'Tarvitsee rauhallisen kodin', '#FF9800', 'warning'),
  (4, 'Sisäkissa', 'Elintapa', 'Sopii sisäkissaksi', '#9C27B0', 'home'),
  (5, 'Sosiaalinen', 'Luonne', 'Tulee toimeen muiden kissojen kanssa', '#795548', 'users');

INSERT INTO cats (id, name, breed, birth_date, gender, image_url, color, weight, arrival_date, description) VALUES
  (1, 'Misu', 'Maine Coon', '2021-05-12', 'FEMALE', 'https://example.com/cats/misu.jpg', 'Ruskea', 5.8, '2025-09-20', 'Rauhallinen ja seurallinen kissa.'),
  (2, 'Naksu', 'Kotimainen lyhytkarva', '2022-11-03', 'MALE', 'https://example.com/cats/naksu.jpg', 'Musta-valkoinen', 4.3, '2026-01-14', 'Leikkisä nuori kissa, joka oppii nopeasti.'),
  (3, 'Helmi', 'Ragdoll', '2020-02-27', 'FEMALE', 'https://example.com/cats/helmi.jpg', 'Vaalea', 6.1, '2025-12-02', 'Kiltti sylikissa, joka viihtyy ihmisten lahella.');

INSERT INTO cat_tags (cat_id, tag_id) VALUES
  (1, 1),
  (1, 4),
  (2, 2),
  (2, 5),
  (3, 1),
  (3, 3);

INSERT INTO health_records (id, last_checkup, veterinarian, weight, vaccinations, notes, cat_id) VALUES
  (1, '2026-02-10', 'Elina Virtanen', 5.8, 'Rabies, Tricat', 'Yleiskunto hyva.', 1),
  (2, '2026-03-01', 'Mika Lehto', 4.3, 'Rabies, Tricat', 'Hammaskivi poistettu.', 2),
  (3, '2026-01-20', 'Sari Niemi', 6.1, 'Rabies, Tricat', 'Ei poikkeavaa.', 3);

INSERT INTO adoption_applications (id, applicant_name, applicant_email, application_date, status, message, cat_id) VALUES
  (1, 'Anna Hakija', 'anna.hakija@example.com', '2026-03-15', 'SUBMITTED', 'Olen etsinyt rauhallista kissaa kerrostaloon.', 1),
  (2, 'Pekka Testaaja', 'pekka.testaaja@example.com', '2026-03-21', 'IN_REVIEW', 'Minulla on aiempaa kokemusta kissoista ja aikaa hoitoon.', 2),
  (3, 'Laura Koti', 'laura.koti@example.com', '2026-03-28', 'APPROVED', 'Haluan tarjota kodin vanhemmalle kissalle.', 3);
