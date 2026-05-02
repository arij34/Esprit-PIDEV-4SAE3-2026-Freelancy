package service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.freelancy.skillmanagement.entity.Skill;
import tn.freelancy.skillmanagement.repository.SkillRepository;
import tn.freelancy.skillmanagement.service.SkillService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    private Skill skill;

    @BeforeEach
    void setUp() {
        skill = new Skill();
        skill.setIdS(1L);
        skill.setName("Java");
        skill.setNormalizedName("java");
    }

    @Test
    void testCreateSkill_ShouldSaveAndReturn() {
        when(skillRepository.save(any(Skill.class)))
                .thenReturn(skill);

        Skill result = skillService.createSkill(skill);

        assertNotNull(result);
        assertEquals("Java", result.getName());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    @Test
    void testGetAllSkills_ShouldReturnList() {
        when(skillRepository.findAll())
                .thenReturn(Arrays.asList(skill));

        List<Skill> result = skillService.getAllSkills();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testGetSkillById_ShouldReturnSkill() {
        when(skillRepository.findById(1L))
                .thenReturn(Optional.of(skill));

        Skill result = skillService.getSkillById(1L);

        assertNotNull(result);
        assertEquals("Java", result.getName());
    }

    @Test
    void testGetSkillById_ShouldReturnNull_WhenNotFound() {
        when(skillRepository.findById(99L))
                .thenReturn(Optional.empty());

        Skill result = skillService.getSkillById(99L);

        assertNull(result);
    }

    @Test
    void testUpdateSkill_ShouldSaveAndReturn() {
        skill.setName("Python");
        when(skillRepository.save(any(Skill.class)))
                .thenReturn(skill);

        Skill result = skillService.updateSkill(skill);

        assertNotNull(result);
        assertEquals("Python", result.getName());
        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    @Test
    void testDeleteSkill_ShouldCallDeleteById() {
        doNothing().when(skillRepository).deleteById(1L);

        skillService.deleteSkill(1L);

        verify(skillRepository, times(1)).deleteById(1L);
    }
}
