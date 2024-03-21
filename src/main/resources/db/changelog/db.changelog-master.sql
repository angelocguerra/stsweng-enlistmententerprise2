-- liquibase formatted sql

-- changeset shem:1711045728687-1
CREATE TABLE "enlistment"."admin" ("id" INTEGER NOT NULL, "firstname" TEXT, "lastname" TEXT, CONSTRAINT "admin_pkey" PRIMARY KEY ("id"));

-- changeset shem:1711045728687-2
CREATE TABLE "enlistment"."faculty" ("faculty_number" INTEGER NOT NULL, "firstname" TEXT, "lastname" TEXT, CONSTRAINT "faculty_pkey" PRIMARY KEY ("faculty_number"));

-- changeset shem:1711045728687-3
CREATE TABLE "enlistment"."faculty_sections" ("faculty_faculty_number" INTEGER NOT NULL, "sections_section_id" TEXT NOT NULL);

-- changeset shem:1711045728687-4
CREATE TABLE "enlistment"."room" ("name" TEXT NOT NULL, "capacity" INTEGER NOT NULL, CONSTRAINT "room_pkey" PRIMARY KEY ("name"));

-- changeset shem:1711045728687-5
CREATE TABLE "enlistment"."room_sections" ("room_name" TEXT NOT NULL, "sections_section_id" TEXT NOT NULL);

-- changeset shem:1711045728687-6
CREATE TABLE "enlistment"."section" ("section_id" TEXT NOT NULL, "number_of_students" INTEGER NOT NULL, "days" INTEGER, "end_time" time(6) WITHOUT TIME ZONE, "start_time" time(6) WITHOUT TIME ZONE, "version" INTEGER DEFAULT 0 NOT NULL, "room_name" TEXT, "subject_subject_id" TEXT, CONSTRAINT "section_pkey" PRIMARY KEY ("section_id"));

-- changeset shem:1711045728687-7
CREATE TABLE "enlistment"."student" ("student_number" INTEGER NOT NULL, "firstname" TEXT, "lastname" TEXT, CONSTRAINT "student_pkey" PRIMARY KEY ("student_number"));

-- changeset shem:1711045728687-8
CREATE TABLE "enlistment"."student_sections" ("student_student_number" INTEGER NOT NULL, "sections_section_id" TEXT NOT NULL);

-- changeset shem:1711045728687-9
CREATE TABLE "enlistment"."student_subjects_taken" ("student_student_number" INTEGER NOT NULL, "subjects_taken_subject_id" TEXT NOT NULL);

-- changeset shem:1711045728687-10
CREATE TABLE "enlistment"."subject" ("subject_id" TEXT NOT NULL, CONSTRAINT "subject_pkey" PRIMARY KEY ("subject_id"));

-- changeset shem:1711045728687-11
CREATE TABLE "enlistment"."subject_prerequisites" ("subject_subject_id" TEXT NOT NULL, "prerequisites_subject_id" TEXT NOT NULL);

-- changeset shem:1711045728687-12
ALTER TABLE "enlistment"."faculty_sections" ADD CONSTRAINT "uk_2ce4o3g0a4rl929cbewtmmu1y" UNIQUE ("sections_section_id");

-- changeset shem:1711045728687-13
ALTER TABLE "enlistment"."room_sections" ADD CONSTRAINT "uk_4y9hvexlvcosyoi6sxolp9if2" UNIQUE ("sections_section_id");

-- changeset shem:1711045728687-14
ALTER TABLE "enlistment"."room_sections" ADD CONSTRAINT "fk18tmp1kdjwlixo8tcyag4xnv" FOREIGN KEY ("sections_section_id") REFERENCES "enlistment"."section" ("section_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-15
ALTER TABLE "enlistment"."faculty_sections" ADD CONSTRAINT "fk2j11yqgbi4wdcyov12kgr5xy0" FOREIGN KEY ("sections_section_id") REFERENCES "enlistment"."section" ("section_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-16
ALTER TABLE "enlistment"."faculty_sections" ADD CONSTRAINT "fk656uje4ggsa8h9sqf9yfji4n" FOREIGN KEY ("faculty_faculty_number") REFERENCES "enlistment"."faculty" ("faculty_number") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-17
ALTER TABLE "enlistment"."subject_prerequisites" ADD CONSTRAINT "fk6ta30r0qhlti2fsjg762emtwb" FOREIGN KEY ("prerequisites_subject_id") REFERENCES "enlistment"."subject" ("subject_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-18
ALTER TABLE "enlistment"."student_sections" ADD CONSTRAINT "fk89rtc8dmim7fd4lulgkecplhj" FOREIGN KEY ("sections_section_id") REFERENCES "enlistment"."section" ("section_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-19
ALTER TABLE "enlistment"."section" ADD CONSTRAINT "fkb7vxfqjj8qa0r38kh4qs1eac1" FOREIGN KEY ("room_name") REFERENCES "enlistment"."room" ("name") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-20
ALTER TABLE "enlistment"."student_sections" ADD CONSTRAINT "fkcbuj7ic505urpqreop2xuwstg" FOREIGN KEY ("student_student_number") REFERENCES "enlistment"."student" ("student_number") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-21
ALTER TABLE "enlistment"."room_sections" ADD CONSTRAINT "fkfd4bv684oyhmv5ntq0gll0exl" FOREIGN KEY ("room_name") REFERENCES "enlistment"."room" ("name") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-22
ALTER TABLE "enlistment"."student_subjects_taken" ADD CONSTRAINT "fkitrqnu6k56yf07ntuo8q1rast" FOREIGN KEY ("subjects_taken_subject_id") REFERENCES "enlistment"."subject" ("subject_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-23
ALTER TABLE "enlistment"."section" ADD CONSTRAINT "fkn23lvqkfvxq9wo0w6qt9xgomd" FOREIGN KEY ("subject_subject_id") REFERENCES "enlistment"."subject" ("subject_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-24
ALTER TABLE "enlistment"."subject_prerequisites" ADD CONSTRAINT "fkogx9mcr8od8y3uf7cxb0spqed" FOREIGN KEY ("subject_subject_id") REFERENCES "enlistment"."subject" ("subject_id") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- changeset shem:1711045728687-25
ALTER TABLE "enlistment"."student_subjects_taken" ADD CONSTRAINT "fkr7ky8jeghxfcdqfugflneppgv" FOREIGN KEY ("student_student_number") REFERENCES "enlistment"."student" ("student_number") ON UPDATE NO ACTION ON DELETE NO ACTION;

-- liquibase formatted sql

-- changeset shem:1711045743861-1
ALTER TABLE enlistment.section ADD instructor_faculty_number INTEGER;

-- changeset shem:1711045743861-2
ALTER TABLE enlistment.section ADD CONSTRAINT "FKhbrcxeiyot9gwvpjd9klgc0rw" FOREIGN KEY (instructor_faculty_number) REFERENCES enlistment.faculty (faculty_number);

-- changeset shem:1711045743861-3
ALTER TABLE enlistment.faculty DROP COLUMN firstname;

-- changeset shem:1711045743861-4
ALTER TABLE enlistment.faculty DROP COLUMN lastname;

