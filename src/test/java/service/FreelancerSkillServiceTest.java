package service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.freelancy.skillmanagement.entity.FreelancerSkill;
import tn.freelancy.skillmanagement.entity.level;
import tn.freelancy.skillmanagement.repository.FreelancerSkillRepository;
import tn.freelancy.skillmanagement.repository.PendingSkillRepository;
import tn.freelancy.skillmanagement.service.FreelancerSkillService;
import tn.freelancy.skillmanagement.service.PendingSkillService;
import tn.freelancy.skillmanagement.service.SimilarityService;
import tn.freelancy.skillmanagement.service.SkillMatcherService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FreelancerSkillServiceTest {

    @Mock
    private FreelancerSkillRepository freelancerSkillRepository;

    @Mock
    private PendingSkillRepository pendingSkillRepository;

    @Mock
    private SkillMatcherService skillMatcherService;

    @Mock
    private PendingSkillService pendingSkillService;

    @Mock
    private SimilarityService similarityService;

    @InjectMocks
    private FreelancerSkillService freelancerSkillService;

    private FreelancerSkill freelancerSkill;

    @BeforeEach
    void setUp() {
        freelancerSkill = new FreelancerSkill();
        freelancerSkill.setUserId(1L);
        freelancerSkill.setYearsExperience(3);
    }

    @Test
    void testCalculateLevel_Beginner() {
        assertEquals(level.BEGINNER,
                freelancerSkillService.calculateLevel(0));
    }

    @Test
    void testCalculateLevel_Elementary() {
        assertEquals(level.ELEMENTARY,
                freelancerSkillService.calculateLevel(1));
    }

    @Test
    void testCalculateLevel_Intermediate() {
        assertEquals(level.INTERMEDIATE,
                freelancerSkillService.calculateLevel(3));
    }

    @Test
    void testCalculateLevel_Advanced() {
        assertEquals(level.ADVANCED,
                freelancerSkillService.calculateLevel(6));
    }

    @Test
    void testCalculateLevel_Expert() {
        assertEquals(level.EXPERT,
                freelancerSkillService.calculateLevel(10));
    }

    @Test
    void testGetAllFreelancerSkills_ShouldReturnList() {
        when(freelancerSkillRepository.findAll())
                .thenReturn(Arrays.asList(freelancerSkill));

        List<FreelancerSkill> result =
                freelancerSkillService.getAllFreelancerSkills();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetFreelancerSkillsByUserId_ShouldReturnList() {
        when(freelancerSkillRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(freelancerSkill));

        List<FreelancerSkill> result =
                freelancerSkillService.getFreelancerSkillsByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetFreelancerSkillById_ShouldReturnNull_WhenNotFound() {
        when(freelancerSkillRepository.findById(99L))
                .thenReturn(java.util.Optional.empty());

        FreelancerSkill result =
                freelancerSkillService.getFreelancerSkillById(99L);

        assertNull(result);
    }

    @Test
    void testUpdateFreelancerSkill_ShouldUpdateLevel() {
        freelancerSkill.setYearsExperience(5);
        when(freelancerSkillRepository.save(any(FreelancerSkill.class)))
                .thenReturn(freelancerSkill);

        FreelancerSkill result =
                freelancerSkillService.updateFreelancerSkill(freelancerSkill);

        assertNotNull(result);
        verify(freelancerSkillRepository, times(1))
                .save(any(FreelancerSkill.class));
    }
}