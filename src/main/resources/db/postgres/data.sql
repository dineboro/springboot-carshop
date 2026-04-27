-- PostgreSQL seed data for weFixCar
-- Converted from MySQL: INSERT IGNORE → INSERT ... ON CONFLICT DO NOTHING
-- ENUM values uppercase to match Java enum names.
-- MySQL inline comments (--) on same line as values removed.

-- ─── Petclinic base data ─────────────────────────────────────────────────────

INSERT INTO vets (first_name, last_name) SELECT 'James',  'Carter'  WHERE NOT EXISTS (SELECT 1 FROM vets WHERE id = 1);
INSERT INTO vets (first_name, last_name) SELECT 'Helen',  'Leary'   WHERE NOT EXISTS (SELECT 1 FROM vets WHERE id = 2);
INSERT INTO vets (first_name, last_name) SELECT 'Linda',  'Douglas' WHERE NOT EXISTS (SELECT 1 FROM vets WHERE id = 3);
INSERT INTO vets (first_name, last_name) SELECT 'Rafael', 'Ortega'  WHERE NOT EXISTS (SELECT 1 FROM vets WHERE id = 4);
INSERT INTO vets (first_name, last_name) SELECT 'Henry',  'Stevens' WHERE NOT EXISTS (SELECT 1 FROM vets WHERE id = 5);
INSERT INTO vets (first_name, last_name) SELECT 'Sharon', 'Jenkins' WHERE NOT EXISTS (SELECT 1 FROM vets WHERE id = 6);

INSERT INTO specialties (name) SELECT 'radiology' WHERE NOT EXISTS (SELECT 1 FROM specialties WHERE name = 'radiology');
INSERT INTO specialties (name) SELECT 'surgery'   WHERE NOT EXISTS (SELECT 1 FROM specialties WHERE name = 'surgery');
INSERT INTO specialties (name) SELECT 'dentistry' WHERE NOT EXISTS (SELECT 1 FROM specialties WHERE name = 'dentistry');

INSERT INTO vet_specialties VALUES (2, 1) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (3, 2) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (3, 3) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (4, 2) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (5, 1) ON CONFLICT (vet_id, specialty_id) DO NOTHING;

INSERT INTO types (name) SELECT 'cat'     WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'cat');
INSERT INTO types (name) SELECT 'dog'     WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'dog');
INSERT INTO types (name) SELECT 'lizard'  WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'lizard');
INSERT INTO types (name) SELECT 'snake'   WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'snake');
INSERT INTO types (name) SELECT 'bird'    WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'bird');
INSERT INTO types (name) SELECT 'hamster' WHERE NOT EXISTS (SELECT 1 FROM types WHERE name = 'hamster');

INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'George',  'Franklin',  '110 W. Liberty St.',     'Madison',   '6085551023' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 1);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Betty',   'Davis',     '638 Cardinal Ave.',      'Sun Prairie','6085551749' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 2);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Eduardo', 'Rodriquez', '2693 Commerce St.',      'McFarland', '6085558763' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 3);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Harold',  'Davis',     '563 Friendly St.',       'Windsor',   '6085553198' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 4);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Peter',   'McTavish',  '2387 S. Fair Way',       'Madison',   '6085552765' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 5);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Jean',    'Coleman',   '105 N. Lake St.',        'Monona',    '6085552654' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 6);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Jeff',    'Black',     '1450 Oak Blvd.',         'Monona',    '6085555387' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 7);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Maria',   'Escobito',  '345 Maple St.',          'Madison',   '6085557683' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 8);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'David',   'Schroeder', '2749 Blackhawk Trail',   'Madison',   '6085559435' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 9);
INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Carlos',  'Estaban',   '2335 Independence La.',  'Waunakee',  '6085555487' WHERE NOT EXISTS (SELECT 1 FROM owners WHERE id = 10);

INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Leo',      '2000-09-07', 1, 1  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 1);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Basil',    '2002-08-06', 6, 2  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 2);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Rosy',     '2001-04-17', 2, 3  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 3);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Jewel',    '2000-03-07', 2, 3  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 4);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Iggy',     '2000-11-30', 3, 4  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 5);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'George',   '2000-01-20', 4, 5  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 6);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Samantha', '1995-09-04', 1, 6  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 7);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Max',      '1995-09-04', 1, 6  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 8);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Lucky',    '1999-08-06', 5, 7  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 9);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Mulligan', '1997-02-24', 2, 8  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 10);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Freddy',   '2000-03-09', 5, 9  WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 11);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Lucky',    '2000-06-24', 2, 10 WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 12);
INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Sly',      '2002-06-08', 1, 10 WHERE NOT EXISTS (SELECT 1 FROM pets WHERE id = 13);

INSERT INTO visits (pet_id, visit_date, description) SELECT 7, '2010-03-04', 'rabies shot' WHERE NOT EXISTS (SELECT 1 FROM visits WHERE id = 1);
INSERT INTO visits (pet_id, visit_date, description) SELECT 8, '2011-03-04', 'rabies shot' WHERE NOT EXISTS (SELECT 1 FROM visits WHERE id = 2);
INSERT INTO visits (pet_id, visit_date, description) SELECT 8, '2009-06-04', 'neutered'    WHERE NOT EXISTS (SELECT 1 FROM visits WHERE id = 3);
INSERT INTO visits (pet_id, visit_date, description) SELECT 7, '2008-09-04', 'spayed'      WHERE NOT EXISTS (SELECT 1 FROM visits WHERE id = 4);

-- ─── Roles & permissions ─────────────────────────────────────────────────────

INSERT INTO roles (name, description) VALUES
  ('ADMIN',        'System administrator with full access.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('MANAGER',      'Shop manager: manages staff, customers, and operations.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('RECEPTIONIST', 'Front desk: handles appointments and customer check-in.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('TECHNICIAN',   'Mechanic: performs service work and updates job status.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('CUSTOMER',     'Portal customer: books appointments and views their vehicles.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('SCHOOL_ADMIN', 'Rec Center Admin: manages facilities, leagues, scores, and users.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name, description) VALUES
  ('STUDENT',      'Student: can join leagues, create teams, and view schedules.')
  ON CONFLICT (name) DO NOTHING;

INSERT INTO permissions (name, description) VALUES
  ('MANAGE_OWN_PROFILE',      'Allows user to update their personal info and password.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('USE_MESSAGING',           'Allows user to send/receive messages with other participants.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('VIEW_LEAGUES',            'Allows user to browse and search available leagues and activities.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('REGISTER_FOR_LEAGUE',     'Allows user to register as an individual for a league.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('CREATE_TEAM',             'Allows user to create a new team as a captain.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('MANAGE_TEAM_INVITATIONS', 'Allows user to accept or decline invitations to a team.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('VIEW_OWN_SCHEDULE',       'Allows user to view their personal and team game schedule.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('VIEW_STANDINGS',          'Allows user to view league standings and team statistics.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('MANAGE_FACILITIES',       'Allows user to C/R/U/D locations, fields, and courts.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('MANAGE_SCHEDULES',        'Allows user to C/R/U/D leagues, activities, and games.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('MANAGE_REGISTRATIONS',    'Allows user to view and approve team registrations.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('MANAGE_SCORES',           'Allows user to enter and confirm game scores.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('SEND_ANNOUNCEMENTS',      'Allows user to send messages to individuals, teams, and leagues.')
  ON CONFLICT (name) DO NOTHING;
INSERT INTO permissions (name, description) VALUES
  ('MANAGE_ALL_SCHOOLS',      'Allows user to create and manage all schools in the system.')
  ON CONFLICT (name) DO NOTHING;

-- ─── Schools ─────────────────────────────────────────────────────────────────

INSERT INTO schools (name, domain, status_id) VALUES ('Kirkwood Community College',  'kirkwood.edu',         'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('University of Iowa',          'uiowa.edu',            'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Iowa State University',       'iastate.edu',          'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('University of Northern Iowa', 'uni.edu',              'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Coe College',                 'coe.edu',              'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Mount Mercy University',      'mtmercy.edu',          'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Drake University',            'drake.edu',            'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Grinnell College',            'grinnell.edu',         'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Luther College',              'luther.edu',           'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Simpson College',             'simpson.edu',          'INACTIVE')  ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Wartburg College',            'wartburg.edu',         'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Cornell College',             'cornellcollege.edu',   'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Loras College',               'loras.edu',            'ACTIVE')    ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('Clarke University',           'clarke.edu',           'SUSPENDED') ON CONFLICT (domain) DO NOTHING;
INSERT INTO schools (name, domain, status_id) VALUES ('St. Ambrose University',      'sau.edu',              'ACTIVE')    ON CONFLICT (domain) DO NOTHING;

-- ─── Locations ───────────────────────────────────────────────────────────────

INSERT INTO locations (school_id, name, description, address, status_id)
  SELECT 1, 'Main Campus', 'The primary campus in Cedar Rapids', '6301 Kirkwood Blvd SW, Cedar Rapids, IA', 'ACTIVE'
  WHERE NOT EXISTS (SELECT 1 FROM locations WHERE school_id = 1 AND name = 'Main Campus');

INSERT INTO locations (school_id, name, description, address, status_id)
  SELECT 2, 'Carver-Hawkeye Arena', 'Main sports arena', '1 Elliott Dr, Iowa City, IA', 'ACTIVE'
  WHERE NOT EXISTS (SELECT 1 FROM locations WHERE school_id = 2 AND name = 'Carver-Hawkeye Arena');

INSERT INTO locations (school_id, parent_location_id, name, description, status_id)
  SELECT 1, (SELECT id FROM locations WHERE school_id = 1 AND name = 'Main Campus'), 'Michael J Gould Rec Center', 'Student recreation facility', 'ACTIVE'
  WHERE NOT EXISTS (SELECT 1 FROM locations WHERE school_id = 1 AND name = 'Michael J Gould Rec Center');

INSERT INTO locations (school_id, parent_location_id, name, description, status_id)
  SELECT 1, (SELECT id FROM locations WHERE school_id = 1 AND name = 'Main Campus'), 'Johnson Hall', 'Athletics building and gymnasium', 'ACTIVE'
  WHERE NOT EXISTS (SELECT 1 FROM locations WHERE school_id = 1 AND name = 'Johnson Hall');

INSERT INTO locations (school_id, parent_location_id, name, description, status_id)
  SELECT 2, (SELECT id FROM locations WHERE school_id = 2 AND name = 'Carver-Hawkeye Arena'), 'Main Court', 'The primary basketball court', 'ACTIVE'
  WHERE NOT EXISTS (SELECT 1 FROM locations WHERE school_id = 2 AND name = 'Main Court');

INSERT INTO locations (school_id, parent_location_id, name, description, status_id)
  SELECT 2, (SELECT id FROM locations WHERE school_id = 2 AND name = 'Carver-Hawkeye Arena'), 'Weight Room', 'Athlete training facility', 'COMING_SOON'
  WHERE NOT EXISTS (SELECT 1 FROM locations WHERE school_id = 2 AND name = 'Weight Room');

-- ─── Subscriptions ───────────────────────────────────────────────────────────

INSERT INTO subscriptions (name, description, monthly_price, annual_price) VALUES
  ('Free', 'Get started with 10 free leagues for your college or university.', 0, 0)
  ON CONFLICT (name) DO NOTHING;
INSERT INTO subscriptions (name, description, monthly_price, annual_price) VALUES
  ('Pro', 'Create up to 25 leagues for your college or university.', 25, 250)
  ON CONFLICT (name) DO NOTHING;

-- ─── Recipes ─────────────────────────────────────────────────────────────────

INSERT INTO recipes (recipe_ingredients, instructions, type, category, dietary_preference, internal_notes) VALUES
  ('Flour, eggs, milk, butter, sugar, baking powder', 'Mix dry ingredients. Add wet ingredients. Bake at 350F for 30 minutes.', 'Baked', 'Dessert', 'Vegetarian', 'Classic vanilla cake recipe')
  ON CONFLICT (internal_notes) DO NOTHING;
INSERT INTO recipes (recipe_ingredients, instructions, type, category, dietary_preference, internal_notes) VALUES
  ('Chicken breast, garlic, olive oil, lemon, herbs', 'Season chicken. Sear in olive oil. Roast at 400F for 25 minutes.', 'Roasted', 'Main Course', 'Gluten-Free', 'Herb roasted chicken')
  ON CONFLICT (internal_notes) DO NOTHING;
INSERT INTO recipes (recipe_ingredients, instructions, type, category, dietary_preference, internal_notes) VALUES
  ('Pasta, tomato sauce, ground beef, onion, garlic', 'Brown beef with onion and garlic. Add tomato sauce. Cook pasta. Combine.', 'Stovetop', 'Main Course', 'None', 'Classic bolognese')
  ON CONFLICT (internal_notes) DO NOTHING;
INSERT INTO recipes (recipe_ingredients, instructions, type, category, dietary_preference, internal_notes) VALUES
  ('Lettuce, tomato, cucumber, red onion, feta, olives, olive oil', 'Chop vegetables. Combine in bowl. Dress with olive oil and salt.', 'Raw', 'Salad', 'Vegetarian', 'Greek salad')
  ON CONFLICT (internal_notes) DO NOTHING;
INSERT INTO recipes (recipe_ingredients, instructions, type, category, dietary_preference, internal_notes) VALUES
  ('Black beans, corn, bell pepper, lime juice, cilantro, cumin', 'Combine all ingredients. Season with cumin and lime juice. Chill before serving.', 'Raw', 'Side Dish', 'Vegan', 'Black bean corn salad')
  ON CONFLICT (internal_notes) DO NOTHING;
