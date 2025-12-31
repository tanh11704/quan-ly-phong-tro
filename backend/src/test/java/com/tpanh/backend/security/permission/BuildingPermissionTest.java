package com.tpanh.backend.security.permission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.security.UserPrincipal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class BuildingPermissionTest {

    private static final String MANAGER_ID = "manager-123";
    private static final String OTHER_MANAGER_ID = "manager-456";
    private static final Integer BUILDING_ID = 1;

    @Mock private BuildingRepository buildingRepository;
    @Mock private Authentication authentication;

    @InjectMocks private BuildingPermission buildingPermission;

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {}

    private void mockUser(String userId, String role) {
        principal = new UserPrincipal(userId, List.of(role));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);
    }

    @Test
    void canAccessBuilding_WhenAdmin_ShouldReturnTrue() {
        mockUser("admin", "ROLE_ADMIN");
        assertTrue(buildingPermission.canAccessBuilding(BUILDING_ID, authentication));
    }

    @Test
    void canAccessBuilding_WhenManagerAndOwnsBuilding_ShouldReturnTrue() {
        mockUser(MANAGER_ID, "ROLE_MANAGER");
        when(buildingRepository.existsByIdAndManagerId(BUILDING_ID, MANAGER_ID)).thenReturn(true);

        assertTrue(buildingPermission.canAccessBuilding(BUILDING_ID, authentication));
        verify(buildingRepository).existsByIdAndManagerId(BUILDING_ID, MANAGER_ID);
    }

    @Test
    void canAccessBuilding_WhenManagerAndDoesNotOwnBuilding_ShouldReturnFalse() {
        mockUser(OTHER_MANAGER_ID, "ROLE_MANAGER");
        when(buildingRepository.existsByIdAndManagerId(BUILDING_ID, OTHER_MANAGER_ID))
                .thenReturn(false);

        assertFalse(buildingPermission.canAccessBuilding(BUILDING_ID, authentication));
    }
}
