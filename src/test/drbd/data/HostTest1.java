package drbd.data;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Map;
import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JPanel;
import java.net.InetAddress;
import drbd.TestSuite1;
import drbd.data.Host;
import drbd.utilities.Tools;

public final class HostTest1 extends TestCase {
    @Before
    protected void setUp() {
        TestSuite1.initTest();
    }

    @After
    protected void tearDown() {
        assertEquals("", TestSuite1.getStdout());
    }

    /* ---- tests ----- */

    @Test
    public void testGetBrowser() {
        for (final Host host : TestSuite1.getHosts()) {
            assertNotNull(host.getBrowser());
        }
    }

    @Test
    public void testGetDrbdColors() {
        final Set<Color> hostColors = new HashSet<Color>();
        for (final Host host : TestSuite1.getHosts()) {
            final Color[] colors = host.getDrbdColors();
            assertNotNull(colors);
            assertTrue(colors.length > 0 && colors.length <= 2);
            assertFalse(hostColors.contains(colors[0]));
            hostColors.add(colors[0]);
        }
    }

    @Test
    public void testGetPmColors() {
        final Set<Color> hostColors = new HashSet<Color>();
        for (final Host host : TestSuite1.getHosts()) {
            final Color[] colors = host.getPmColors();
            assertNotNull(colors);
            assertTrue(colors.length > 0 && colors.length <= 2);
            assertFalse(hostColors.contains(colors[0]));
            hostColors.add(colors[0]);
        }
    }

    @Test
    public void testSetClStatus() {
        for (final Host host : TestSuite1.getHosts()) {
            host.setClStatus(false);
            Tools.sleep(500);
            host.setClStatus(true);
            Tools.sleep(500);
        }
    }

    @Test
    public void testSetDrbdStatus() {
        for (final Host host : TestSuite1.getHosts()) {
            host.setDrbdStatus(false);
            Tools.sleep(500);
            host.setDrbdStatus(true);
            Tools.sleep(500);
        }
    }

    @Test
    public void testIsClStatus() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.isClStatus());
        }
    }

    @Test
    public void testIsDrbdStatus() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.isDrbdStatus());
        }
    }

    @Test
    public void testIsInCluster() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.isInCluster());
            assertTrue(host.isInCluster(null));
            assertTrue(host.isInCluster(new drbd.data.Cluster()));
        }
    }

    @Test
    public void testGetNetInterfaces() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getNetInterfaces().length > 0);
            assertNotNull(host.getNetInterfaces()[0]);
            assertTrue(TestSuite1.noValueIsNull(host.getNetInterfaces()));
        }
    }

    @Test
    public void testGetBridges() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getBridges().size() == 0);
            assertTrue(TestSuite1.noValueIsNull(host.getBridges()));
        }
    }

    @Test
    public void testGetBlockDevices() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getBlockDevices().length > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getBlockDevices()));
        }
    }

    @Test
    public void testGetBlockDeviceNames() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getBlockDevicesNames().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getBlockDevicesNames()));
            for (final String bd : host.getBlockDevicesNames()) {
                assertTrue(host.getBlockDevice(bd) != null);
            }
        }
    }

    @Test
    public void testGetBlockDeviceNamesIntersection() {
        List<String> otherBlockDevices = null;
        for (final Host host : TestSuite1.getHosts()) {
            otherBlockDevices =
                    host.getBlockDevicesNamesIntersection(otherBlockDevices);
            assertTrue(TestSuite1.noValueIsNull(otherBlockDevices));
        }
        if (TestSuite1.getHosts().size() > 0) {
            assertTrue(otherBlockDevices.size() > 0);
        }
    }

    @Test
    public void testGetNetworkIps() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getNetworkIps().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getNetworkIps()));
        }
    }

    @Test
    public void testGetNetworksIntersection() {
        Map<String, String> otherNetworks = null;
        for (final Host host : TestSuite1.getHosts()) {
            otherNetworks = host.getNetworksIntersection(otherNetworks);
            assertTrue(TestSuite1.noValueIsNull(otherNetworks));
        }
        if (TestSuite1.getHosts().size() > 0) {
            assertTrue(otherNetworks.size() > 0);
        }
    }

    @Test
    public void testGetIpsFromNetwork() {
        for (final Host host : TestSuite1.getHosts()) {
            final List<String> ips = host.getIpsFromNetwork("192.168.122.0");
            assertTrue(ips.size() > 0);
            assertTrue(TestSuite1.noValueIsNull(ips));
            for (final String ip : ips) {
                assertTrue(ip.startsWith("192.168.122."));
            }
        }
    }

    @Test
    public void testGetFileSystems() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getFileSystems().length > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getFileSystems()));

            assertTrue(host.getFileSystemsList().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getFileSystemsList()));
        }
    }

    @Test
    public void testGetCryptoModules() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getCryptoModules().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getCryptoModules()));
        }
    }

    @Test
    public void testGetQemuKeymaps() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getQemuKeymaps().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getQemuKeymaps()));
        }
    }

    @Test
    public void testGetCPUMapsModels() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getCPUMapModels().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getCPUMapModels()));
        }
    }

    @Test
    public void testGetCPUMapVendor() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getCPUMapVendors().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getCPUMapVendors()));
        }
    }

    @Test
    public void testGetMountPointsList() {
        for (final Host host : TestSuite1.getHosts()) {
            assertTrue(host.getMountPointsList().size() > 0);
            assertTrue(TestSuite1.noValueIsNull(host.getMountPointsList()));
        }
    }

    @Test
    public void testGetAvailableDrbdVersions() {
        for (final Host host : TestSuite1.getHosts()) {
            assertNull(host.getAvailableDrbdVersions());
        }
    }
}
