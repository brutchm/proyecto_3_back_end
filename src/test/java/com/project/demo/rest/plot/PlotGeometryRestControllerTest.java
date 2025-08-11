package com.project.demo.rest.plot;

import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.plot.FarmPlot;
import com.project.demo.logic.entity.plot.FarmPlotRepository;
import com.project.demo.logic.entity.plot.PlotGeometry;
import com.project.demo.logic.entity.plot.PlotGeometryRepository;
import com.project.demo.logic.entity.userfarm.UserFarmId;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

@WebMvcTest(PlotGeometryRestController.class)
@AutoConfigureMockMvc(addFilters = false)

public class PlotGeometryRestControllerTest {
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PlotGeometryRepository geometryRepository;
    @MockBean
    private FarmPlotRepository farmPlotRepository;
    @MockBean
    private UserXFarmRepository userXFarmRepository;

    @Test
    void testGetGeometryByPlotSuccess() throws Exception {
        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Farm mockFarm = new Farm();
        mockFarm.setId(1L);
        FarmPlot plot = new FarmPlot();
        plot.setId(1L);
        plot.setFarm(mockFarm);
        PlotGeometry geometry = new PlotGeometry();
        geometry.setId(1L);
        geometry.setFarmPlot(plot);
        Mockito.when(farmPlotRepository.findById(1L)).thenReturn(Optional.of(plot));
        Mockito.when(geometryRepository.findByFarmPlot_Id(1L)).thenReturn(Optional.of(geometry));
        Mockito.when(userXFarmRepository.existsById(Mockito.any())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/plots/1/geometry")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testCreateGeometrySuccess() throws Exception {
        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Farm mockFarm = new Farm();
        mockFarm.setId(1L);
        FarmPlot plot = new FarmPlot();
        plot.setId(1L);
        plot.setFarm(mockFarm);
        Mockito.when(farmPlotRepository.findById(1L)).thenReturn(Optional.of(plot));
        Mockito.when(userXFarmRepository.existsById(Mockito.any())).thenReturn(true);
        Mockito.when(geometryRepository.findByFarmPlot_Id(1L)).thenReturn(Optional.empty());
        Mockito.when(geometryRepository.save(Mockito.any(PlotGeometry.class))).thenReturn(new PlotGeometry());
        String geoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,1],[2,2],[0,0]]]}";
        mockMvc.perform(MockMvcRequestBuilders.post("/plots/1/geometry")
            .contentType(MediaType.APPLICATION_JSON)
            .content(geoJson))
            .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void testCreateGeometryPlotNotFound() throws Exception {
        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(farmPlotRepository.findById(99L)).thenReturn(Optional.empty());
        String geoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,1],[2,2],[0,0]]]}";
        mockMvc.perform(MockMvcRequestBuilders.post("/plots/99/geometry")
            .contentType(MediaType.APPLICATION_JSON)
            .content(geoJson))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testCreateGeometryAlreadyExists() throws Exception {
        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Farm mockFarm = new Farm();
        mockFarm.setId(1L);
        FarmPlot plot = new FarmPlot();
        plot.setId(1L);
        plot.setFarm(mockFarm);
        Mockito.when(farmPlotRepository.findById(1L)).thenReturn(Optional.of(plot));
        Mockito.when(userXFarmRepository.existsById(Mockito.any(UserFarmId.class))).thenReturn(true);
        Mockito.when(geometryRepository.findByFarmPlot_Id(1L)).thenReturn(Optional.of(new PlotGeometry()));
        String geoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,1],[2,2],[0,0]]]}";
        mockMvc.perform(MockMvcRequestBuilders.post("/plots/1/geometry")
            .contentType(MediaType.APPLICATION_JSON)
            .content(geoJson))
            .andExpect(MockMvcResultMatchers.status().isConflict());
    }
}
