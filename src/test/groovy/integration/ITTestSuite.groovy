package integration

import integration.test.ZMain
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([
        ZMain.class,
])

public class ITTestSuite {

}