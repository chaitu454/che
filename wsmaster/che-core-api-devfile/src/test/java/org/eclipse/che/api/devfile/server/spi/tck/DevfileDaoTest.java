package org.eclipse.che.api.devfile.server.spi.tck;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.devfile.server.model.impl.PersistentDevfileImpl;
import org.eclipse.che.api.devfile.server.spi.DevfileDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
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

import javax.inject.Inject;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

@Listeners(TckListener.class)
@Test(suiteName = DevfileDaoTest.SUITE_NAME)
public class DevfileDaoTest {
  public static final String SUITE_NAME = "DevfileDaoTck";
  private static final int ENTRY_COUNT = 5;

  private PersistentDevfileImpl[] devfiles;
  private UserImpl[] users;

  @Inject private DevfileDao devfileDaoDao;

  @Inject private TckRepository<PersistentDevfileImpl> devfileTckRepository;

  @Inject private TckRepository<UserImpl> userTckRepository;

  @BeforeMethod
  public void setUp() throws Exception {
    devfiles = new PersistentDevfileImpl[ENTRY_COUNT];
    users = new UserImpl[ENTRY_COUNT];
    for (int i = 0; i < ENTRY_COUNT; i++) {
      users[i] = new UserImpl("userId_" + i, "email_" + i, "name" + i);
    }
    for (int i = 0; i < ENTRY_COUNT; i++) {
      devfiles[i] =
          createPersistentDevfile(
              NameGenerator.generate("id-", 6), NameGenerator.generate("devfileName", 6));
    }
    userTckRepository.createAll(Arrays.asList(users));
    devfileTckRepository.createAll(
        Stream.of(devfiles).map(PersistentDevfileImpl::new).collect(toList()));
  }

  @AfterMethod
  public void cleanUp() throws Exception {
    devfileTckRepository.removeAll();
    userTckRepository.removeAll();
  }

  @Test
  public void shouldGetPersistentDevfileById() throws Exception {
    final PersistentDevfileImpl devfile = devfiles[0];

    assertEquals(devfileDaoDao.getById(devfile.getId()), devfile);
  }

  private static PersistentDevfileImpl createPersistentDevfile(String id, String name) {
    return new PersistentDevfileImpl(id, createDevfile(name));
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

    org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl command1 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl(
            name + "-1", singletonList(action1), singletonMap("attr1", "value1"), null);
    org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl command2 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl(
            name + "-2", singletonList(action2), singletonMap("attr2", "value2"), null);

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
