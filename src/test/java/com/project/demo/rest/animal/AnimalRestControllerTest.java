
package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.Animal;
import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.userfarm.UserXFarmRepository;
import com.project.demo.logic.entity.animal.AnimalGroupRepository;
import com.project.demo.logic.entity.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.Optional;

@WebMvcTest(AnimalRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnimalRestControllerTest {
    @Test
    void testGetAnimalsByFarmSuccess() throws Exception {
        Farm farm = new Farm();
        farm.setId(1L);
        Animal animal1 = new Animal();
        animal1.setId(1L);
        animal1.setSpecies("Cow");
        animal1.setFarm(farm);
        Animal animal2 = new Animal();
        animal2.setId(2L);
        animal2.setSpecies("Sheep");
        animal2.setFarm(farm);
        java.util.List<Animal> animals = java.util.Arrays.asList(animal1, animal2);
        Mockito.when(farmRepository.findById(1L)).thenReturn(Optional.of(farm));
        Mockito.when(animalRepository.findByFarmId(1L)).thenReturn(animals);
        Mockito.when(userXFarmRepository.existsById(Mockito.any())).thenReturn(true);

        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.get("/farms/1/animals")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AnimalRepository animalRepository;
    @MockBean
    private FarmRepository farmRepository;
    @MockBean
    private UserXFarmRepository userXFarmRepository;
    @MockBean
    private AnimalGroupRepository animalGroupRepository;
    @MockBean
    private com.project.demo.logic.entity.auth.JwtService jwtService;
        @MockBean
        private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void testCreateAnimalSuccess() throws Exception {
        Animal animal = new Animal();
        animal.setSpecies("Cow");
        Farm farm = new Farm();
        farm.setId(1L);
        Mockito.when(farmRepository.findById(1L)).thenReturn(Optional.of(farm));
        Mockito.when(animalRepository.save(Mockito.any(Animal.class))).thenReturn(animal);
        Mockito.when(userXFarmRepository.existsById(Mockito.any())).thenReturn(true);
        String json = "{\"species\":\"Cow\"}";

        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.post("/farms/1/animals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void testCreateAnimalFarmNotFound() throws Exception {
        Mockito.when(farmRepository.findById(99L)).thenReturn(Optional.empty());
        Mockito.when(userXFarmRepository.existsById(Mockito.any())).thenReturn(true);
        String json = "{\"species\":\"Cow\"}";

        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.post("/farms/99/animals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testCreateAnimalAccessDenied() throws Exception {
        Mockito.when(userXFarmRepository.existsById(Mockito.any())).thenReturn(false);
        String json = "{\"species\":\"Cow\"}";

        // Set up custom User principal in SecurityContext
        User mockUser = new User();
        mockUser.setId(1L);
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(MockMvcRequestBuilders.post("/farms/1/animals")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
