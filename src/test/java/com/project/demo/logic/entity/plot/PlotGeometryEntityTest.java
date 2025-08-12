
package com.project.demo.logic.entity.plot;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import com.project.demo.logic.entity.plot.PlotGeometry;
import com.project.demo.logic.entity.plot.FarmPlot;

public class PlotGeometryEntityTest {
    @Test
    void testSetAndGetProperties() {
    PlotGeometry geometry = new PlotGeometry();
    geometry.setId(10L);
    FarmPlot plot = new FarmPlot();
    plot.setId(5L);
    geometry.setFarmPlot(plot);
    geometry.setGeometryPolygon("POLYGON((0 0,1 1,2 2,0 0))");
    geometry.setCreatedAt(LocalDateTime.now());
    geometry.setUpdatedAt(LocalDateTime.now());

    assertEquals(10L, geometry.getId());
    assertEquals(plot, geometry.getFarmPlot());
    assertEquals("POLYGON((0 0,1 1,2 2,0 0))", geometry.getGeometryPolygon());
    assertNotNull(geometry.getCreatedAt());
    assertNotNull(geometry.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
    PlotGeometry geometry1 = new PlotGeometry();
    geometry1.setId(1L);
    PlotGeometry geometry2 = new PlotGeometry();
    geometry2.setId(1L);
    PlotGeometry geometry3 = new PlotGeometry();
    geometry3.setId(2L);

    // By default, different instances are not equal unless equals/hashCode is overridden
    assertNotEquals(geometry1, geometry2);
    assertNotEquals(geometry1, geometry3);
    }
}
