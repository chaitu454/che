--
-- Copyright (c) 2012-2020 Red Hat, Inc.
-- This program and the accompanying materials are made
-- available under the terms of the Eclipse Public License 2.0
-- which is available at https://www.eclipse.org/legal/epl-2.0/
--
-- SPDX-License-Identifier: EPL-2.0
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--


-- add persistentdevfile table
CREATE TABLE persistentdevfile (
    id          VARCHAR(255)    NOT NULL UNIQUE,
    devfile_id  VARCHAR(255)    NOT NULL UNIQUE,
    PRIMARY KEY (id)
);
CREATE INDEX index_persistentdevfile_devfile_id ON persistentdevfile (devfile_id);
ALTER TABLE persistentdevfile ADD CONSTRAINT fk_persistentdevfile_devfile_id FOREIGN KEY (devfile_id) REFERENCES devfile (id);
