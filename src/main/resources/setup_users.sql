INSERT INTO users (id, email, name, password, role)
SELECT * FROM (VALUES (gen_random_uuid(), 'test1@teamnoco.de', 'Пользователь 1',
                       '$2a$10$6mPhkIVJtEDZhThMeMtZT.SxSF7PnkEs5Mexkq.123XwK8yMCdFVO', 'USER'),
                      (gen_random_uuid(), 'test2@teamnoco.de', 'Пользователь 2',
                       '$2a$10$UQf7WZlQJbl/NxvWlqv1kuF72kLcHMvMp2lekN2Pm3Aq2qpiHb3UW', 'USER'),
                      (gen_random_uuid(), 'test3@teamnoco.de', 'Пользователь 3',
                       '$2a$10$r1eTJ.FrDlG6UxjA75KvQOlLT2wzD.npWz4oHFwoU0LKJ6zDYRm8m', 'USER'),
                      (gen_random_uuid(), 'test4@teamnoco.de', 'Пользователь 4',
                       '$2a$10$pyJrEz5PiwTjHyTc1.yQOe0jAvcVGquayOLDpB2PliFGYMYeDov1e', 'USER'),
                      (gen_random_uuid(), 'test5@teamnoco.de', 'Пользователь 5',
                       '$2a$10$7Sk3Sl.uuWlKjLW50JNFj.FYY4EA/6.Wfu7sQYmZjMVg7B2J50ND6', 'USER'),
                      (gen_random_uuid(), 'test6@teamnoco.de', 'Пользователь 6',
                       '$2a$10$5zTpxru.vPrYpFgBAtw3w.0Jsc3VAbipOxPZ8uh7Y6mL2W7ovG/Tq', 'USER'),
                      (gen_random_uuid(), 'test7@teamnoco.de', 'Пользователь 7',
                       '$2a$10$hqoIOlIHjZrw0.fZse5Xc.3qpUuSaFqD6.E0XqnjqjSobXe6ytU6W', 'USER'),
                      (gen_random_uuid(), 'test8@teamnoco.de', 'Пользователь 8',
                       '$2a$10$HoGuBudlivwV2o8qcmjtT.dP8ECm5/E7hOBqNnV6s70MSz60jvBbC', 'USER'),
                      (gen_random_uuid(), 'test9@teamnoco.de', 'Пользователь 9',
                       '$2a$10$AK.qgkGfKMkVJCQDQUqHweY.C/uBoY/kUC7a4AnBQhBsf7JaX3LRS', 'USER'),
                      (gen_random_uuid(), 'test10@teamnoco.de', 'Пользователь 10',
                       '$2a$10$N39e/WQhaA6nWXxaSW90g.dl3GjoOTQLFCO5CpFOkEtSOi3AHdjqG', 'USER'),
                   (gen_random_uuid(), 'admin@teamnoco.de', 'Администратор', '$2a$10$WMTGiNCM7IvmxBmkRqg2Q.Ly81tMr4ZOHQgUfhucpx2YhZJfZN.ge', 'ADMIN')
              ) AS tmp (id, email, name, password, role)
WHERE NOT EXISTS (SELECT 1 FROM users);
