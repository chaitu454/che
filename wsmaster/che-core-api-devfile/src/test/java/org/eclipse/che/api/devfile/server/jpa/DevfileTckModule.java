/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server.jpa;

import com.google.inject.TypeLiteral;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.devfile.server.model.impl.PersistentDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.DevfileDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.devfile.SerializableConverter;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl;
import org.eclipse.che.commons.test.db.H2DBTestServer;
import org.eclipse.che.commons.test.db.H2JpaCleaner;
import org.eclipse.che.commons.test.db.PersistTestModuleBuilder;
import org.eclipse.che.commons.test.tck.TckModule;
import org.eclipse.che.commons.test.tck.TckResourcesCleaner;
import org.eclipse.che.commons.test.tck.repository.JpaTckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.core.db.h2.jpa.eclipselink.H2ExceptionHandler;
import org.eclipse.che.core.db.schema.SchemaInitializer;
import org.eclipse.che.core.db.schema.impl.flyway.FlywaySchemaInitializer;
import org.h2.Driver;

// import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
// import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
// import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
// import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
// import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
// import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
// import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
// import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
// import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
// import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

/**
 * Tck module for factory test.
 *
 * @author Yevhenii Voevodin
 */
public class DevfileTckModule extends TckModule {

  @Override
  protected void configure() {
    H2DBTestServer server = H2DBTestServer.startDefault();
    install(
        new PersistTestModuleBuilder()
            .setDriver(Driver.class)
            .runningOn(server)
            .addEntityClasses(
                UserImpl.class,
                AccountImpl.class,
                PersistentDevfileImpl.class,
                DevfileImpl.class,
                ActionImpl.class,
                CommandImpl.class,
                ComponentImpl.class,
                DevfileImpl.class,
                EndpointImpl.class,
                EntrypointImpl.class,
                EnvImpl.class,
                ProjectImpl.class,
                SourceImpl.class,
                VolumeImpl.class)
            //            .addEntityClass(
            //
            // "org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl$Attribute")
            .addClass(SerializableConverter.class)
            .setExceptionHandler(H2ExceptionHandler.class)
            .setProperty("eclipselink.logging.level", "OFF")
            .build());
    bind(DBInitializer.class).asEagerSingleton();
    bind(SchemaInitializer.class)
        .toInstance(new FlywaySchemaInitializer(server.getDataSource(), "che-schema"));
    bind(TckResourcesCleaner.class).toInstance(new H2JpaCleaner(server));

    bind(DevfileDao.class).to(JpaDevfileDao.class);

    bind(new TypeLiteral<TckRepository<PersistentDevfileImpl>>() {})
        .toInstance(new JpaTckRepository<>(PersistentDevfileImpl.class));
    bind(new TypeLiteral<TckRepository<UserImpl>>() {})
        .toInstance(new JpaTckRepository<>(UserImpl.class));
  }
}
