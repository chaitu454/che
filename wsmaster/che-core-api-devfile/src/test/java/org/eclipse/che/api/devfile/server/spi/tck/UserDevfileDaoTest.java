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
package org.eclipse.che.api.devfile.server.spi.tck;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.UserDevfileDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TckListener.class)
@Test(suiteName = UserDevfileDaoTest.SUITE_NAME)
public class UserDevfileDaoTest {
  public static final String SUITE_NAME = "DevfileDaoTck";
  private static final int ENTRY_COUNT = 5;

  private UserDevfileImpl[] devfiles;
  private UserImpl[] users;

  @Inject private UserDevfileDao userDevfileDaoDao;

  @Inject private TckRepository<UserDevfileImpl> devfileTckRepository;

  @Inject private TckRepository<UserImpl> userTckRepository;

  @BeforeMethod
  public void setUp() throws Exception {
    devfiles = new UserDevfileImpl[ENTRY_COUNT];
    users = new UserImpl[ENTRY_COUNT];
    for (int i = 0; i < ENTRY_COUNT; i++) {
      users[i] = new UserImpl("userId_" + i, "email_" + i, "name" + i);
    }
    for (int i = 0; i < ENTRY_COUNT; i++) {
      devfiles[i] =
          createUserDevfile(
              NameGenerator.generate("id-", 6), NameGenerator.generate("devfileName", 6));
    }
    userTckRepository.createAll(Arrays.asList(users));
    devfileTckRepository.createAll(Stream.of(devfiles).map(UserDevfileImpl::new).collect(toList()));
  }

  @AfterMethod
  public void cleanUp() throws Exception {
    devfileTckRepository.removeAll();
    userTckRepository.removeAll();
  }

  @Test
  public void shouldGetUserDevfileById() throws Exception {
    final UserDevfileImpl devfile = devfiles[0];

    assertEquals(userDevfileDaoDao.getById(devfile.getId()), devfile);
  }

  @Test(dependsOnMethods = "shouldGetUserDevfileById")
  public void shouldCreateUserDevfile() throws Exception {
    final UserDevfileImpl devfile = createUserDevfile();
    userDevfileDaoDao.create(devfile);

    assertEquals(userDevfileDaoDao.getById(devfile.getId()), new UserDevfileImpl(devfile));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenCreateNullDevfile() throws Exception {
    userDevfileDaoDao.create(null);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenCreatingUserDevfileWithExistingId() throws Exception {
    // given
    final UserDevfileImpl devfile = createUserDevfile();
    final UserDevfileImpl existing = devfiles[0];
    devfile.setId(existing.getId());
    // when
    userDevfileDaoDao.create(devfile);
    // then
  }

  @Test
  public void shouldUpdateUserDevfile() throws Exception {
    // given
    final UserDevfileImpl update = devfiles[0];
    update.setApiVersion("V15.0");
    update.setProjects(
        ImmutableList.of(
            new ProjectImpl(
                "projectUp2",
                new SourceImpl(
                    "typeUp2",
                    "http://location",
                    "branch2",
                    "point2",
                    "tag2",
                    "commit2",
                    "sparseCheckoutDir2"),
                "path2")));
    update.setComponents(ImmutableList.of(new ComponentImpl("type3", "id54")));
    update.setCommands(
        ImmutableList.of(
            new CommandImpl(
                new CommandImpl(
                    "cmd1",
                    singletonList(
                        new ActionImpl(
                            "exe44", "compo2nent2", "run.sh", "/home/user/2", null, null)),
                    singletonMap("attr1", "value1"),
                    null))));
    update.setAttributes(ImmutableMap.of("key2", "val34"));
    update.setMetadata(new MetadataImpl("myNewName"));
    // when
    userDevfileDaoDao.update(update);
    // then
    assertEquals(userDevfileDaoDao.getById(update.getId()), update);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldNotUpdateWorkspaceWhichDoesNotExist() throws Exception {
    // given
    final UserDevfileImpl userDevfile = devfiles[0];
    userDevfile.setId("non-existing-devfile");
    // when
    userDevfileDaoDao.update(userDevfile);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingNull() throws Exception {
    userDevfileDaoDao.update(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGetByIdNull() throws Exception {
    userDevfileDaoDao.getById(null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenDeleteNull() throws Exception {
    userDevfileDaoDao.getById(null);
  }

  @Test(expectedExceptions = NotFoundException.class, dependsOnMethods = "shouldGetUserDevfileById")
  public void shouldRemoveDevfile() throws Exception {
    final String userDevfileId = devfiles[0].getId();
    userDevfileDaoDao.remove(userDevfileId);
    userDevfileDaoDao.getById(userDevfileId);
  }

  @Test
  public void shouldDoNothingWhenRemovingNonExistingUserDevfile() throws Exception {
    userDevfileDaoDao.remove("non-existing");
  }

  private static UserDevfileImpl createUserDevfile() {
    return createUserDevfile(NameGenerator.generate("name", 6));
  }

  private static UserDevfileImpl createUserDevfile(String name) {
    return createUserDevfile(NameGenerator.generate("id", 6), name);
  }

  private static UserDevfileImpl createUserDevfile(String id, String name) {
    return new UserDevfileImpl(id, createDevfile(name));
  }

  private static DevfileImpl createDevfile(String name) {

    SourceImpl source1 =
        new SourceImpl(
            "type1",
            "http://location",
            "branch1",
            "point1",
            "tag1",
            "commit1",
            "sparseCheckoutDir1");
    ProjectImpl project1 = new ProjectImpl("project1", source1, "path1");

    SourceImpl source2 =
        new SourceImpl(
            "type2",
            "http://location",
            "branch2",
            "point2",
            "tag2",
            "commit2",
            "sparseCheckoutDir2");
    ProjectImpl project2 = new ProjectImpl("project2", source2, "path2");

    ActionImpl action1 =
        new ActionImpl("exec1", "component1", "run.sh", "/home/user/1", null, null);
    ActionImpl action2 =
        new ActionImpl("exec2", "component2", "run.sh", "/home/user/2", null, null);

    CommandImpl command1 =
        new CommandImpl(name + "-1", singletonList(action1), singletonMap("attr1", "value1"), null);
    CommandImpl command2 =
        new CommandImpl(name + "-2", singletonList(action2), singletonMap("attr2", "value2"), null);

    EntrypointImpl entrypoint1 =
        new EntrypointImpl(
            "parentName1",
            singletonMap("parent1", "selector1"),
            "containerName1",
            asList("command1", "command2"),
            asList("arg1", "arg2"));

    EntrypointImpl entrypoint2 =
        new EntrypointImpl(
            "parentName2",
            singletonMap("parent2", "selector2"),
            "containerName2",
            asList("command3", "command4"),
            asList("arg3", "arg4"));

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume1 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name1", "path1");

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume2 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name2", "path2");

    EnvImpl env1 = new EnvImpl("name1", "value1");
    EnvImpl env2 = new EnvImpl("name2", "value2");

    EndpointImpl endpoint1 = new EndpointImpl("name1", 1111, singletonMap("key1", "value1"));
    EndpointImpl endpoint2 = new EndpointImpl("name2", 2222, singletonMap("key2", "value2"));

    ComponentImpl component1 =
        new ComponentImpl(
            "kubernetes",
            "component1",
            "eclipse/che-theia/0.0.1",
            ImmutableMap.of("java.home", "/home/user/jdk11"),
            "https://mysite.com/registry/somepath1",
            "/dev.yaml",
            "refcontent1",
            ImmutableMap.of("app.kubernetes.io/component", "db"),
            asList(entrypoint1, entrypoint2),
            "image",
            "256G",
            "128M",
            "2",
            "130m",
            false,
            singletonList("command"),
            singletonList("arg"),
            asList(volume1, volume2),
            asList(env1, env2),
            asList(endpoint1, endpoint2));
    component1.setSelector(singletonMap("key1", "value1"));

    ComponentImpl component2 =
        new ComponentImpl(
            "kubernetes",
            "component2",
            "eclipse/che-theia/0.0.1",
            ImmutableMap.of(
                "java.home",
                "/home/user/jdk11aertwertert",
                "java.boolean",
                true,
                "java.integer",
                123444),
            "https://mysite.com/registry/somepath2",
            "/dev.yaml",
            "refcontent2",
            ImmutableMap.of("app.kubernetes.io/component", "webapp"),
            asList(entrypoint1, entrypoint2),
            "image",
            "256G",
            "256M",
            "3",
            "180m",
            false,
            singletonList("command"),
            singletonList("arg"),
            asList(volume1, volume2),
            asList(env1, env2),
            asList(endpoint1, endpoint2));
    component2.setSelector(singletonMap("key2", "value2"));

    DevfileImpl devfile =
        new DevfileImpl(
            "0.0.1",
            asList(project1, project2),
            asList(component1, component2),
            asList(command1, command2),
            singletonMap("attribute1", "value1"),
            new MetadataImpl(name));

    return devfile;
  }
}
