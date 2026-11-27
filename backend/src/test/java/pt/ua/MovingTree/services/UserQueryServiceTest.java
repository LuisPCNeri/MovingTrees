package pt.ua.MovingTree.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import pt.ua.MovingTree.dtos.PagedResponse;
import pt.ua.MovingTree.dtos.UserFilter;
import pt.ua.MovingTree.dtos.UserResponse;
import pt.ua.MovingTree.entities.AppUser;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.enums.AppUserStatus;
import pt.ua.MovingTree.exceptions.InvalidPagingException;
import pt.ua.MovingTree.repositories.AppUserRepository;
import pt.ua.MovingTree.security.CallerGuard;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private CallerGuard callerGuard;
    @Mock
    private Root<AppUser> root;
    @Mock
    private CriteriaQuery<AppUser> query;
    @Mock
    private CriteriaBuilder cb;
    @Mock
    private Path<Object> municipalityPath;
    @Mock
    private Path<Object> municipalityIdPath;
    @Mock
    private Path<Object> rolePath;
    @Mock
    private Path<Object> teamPath;
    @Mock
    private Path<Object> teamIdPath;
    @Mock
    private Path<Object> statusPath;
    @Mock
    private Path<String> namePath;
    @Mock
    private Expression<String> lowerName;
    @Mock
    private Predicate municipalityPredicate;
    @Mock
    private Predicate rolePredicate;
    @Mock
    private Predicate teamPredicate;
    @Mock
    private Predicate statusPredicate;
    @Mock
    private Predicate namePredicate;
    @Mock
    private Predicate combinedPredicate;

    private UserQueryService service;

    //passa, todos os filtros são compostos com AND e o município da sessão é sempre incluído
    @Test
    @SuppressWarnings("unchecked")
    void composesAllFiltersForCallerMunicipality() {
        service = new UserQueryService(userRepository, callerGuard);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        ArgumentCaptor<Specification<AppUser>> specification = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAll(specification.capture(), pageable.capture()))
                .thenReturn(new PageImpl<>(List.of()));
        stubPredicates();

        service.list(0, 20, new UserFilter(AppUserRole.FIELD_WORKER, 30L, AppUserStatus.ACTIVE, " ana "));

        Predicate result = specification.getValue().toPredicate(root, query, cb);
        assertEquals(combinedPredicate, result);
        verify(cb).and(municipalityPredicate, rolePredicate, teamPredicate, statusPredicate, namePredicate);
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(20, pageable.getValue().getPageSize());
        assertEquals(Sort.by("id"), pageable.getValue().getSort());
    }

    //passa, o limite máximo de page size é aceite
    @Test
    @SuppressWarnings("unchecked")
    void acceptsMaximumPageSize() {
        service = new UserQueryService(userRepository, callerGuard);
        when(callerGuard.requireMunicipalAdministrator()).thenReturn(10L);
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        when(userRepository.findAll(any(Specification.class), pageable.capture())).thenReturn(
                new PageImpl<>(List.of(), PageRequest.of(2, UserQueryService.MAX_PAGE_SIZE), 0));

        PagedResponse<UserResponse> response = service.list(
                2, UserQueryService.MAX_PAGE_SIZE, new UserFilter(null, null, null, null));

        assertEquals(UserQueryService.MAX_PAGE_SIZE, response.size());
        assertEquals(UserQueryService.MAX_PAGE_SIZE, pageable.getValue().getPageSize());
    }

    //falha, page size zero deve ser rejeitado antes de consultar o repositório
    @Test
    void rejectsZeroPageSize() {
        service = new UserQueryService(userRepository, callerGuard);

        assertThrows(InvalidPagingException.class,
                () -> service.list(0, 0, new UserFilter(null, null, null, null)));

        verifyNoInteractions(callerGuard, userRepository);
    }

    //falha, page size acima do limite deve ser rejeitado
    @Test
    void rejectsPageSizeAboveLimit() {
        service = new UserQueryService(userRepository, callerGuard);

        assertThrows(InvalidPagingException.class,
                () -> service.list(0, UserQueryService.MAX_PAGE_SIZE + 1, new UserFilter(null, null, null, null)));

        verifyNoInteractions(callerGuard, userRepository);
    }

    //falha, página negativa deve ser rejeitada
    @Test
    void rejectsNegativePage() {
        service = new UserQueryService(userRepository, callerGuard);

        assertThrows(InvalidPagingException.class,
                () -> service.list(-1, 20, new UserFilter(null, null, null, null)));

        verifyNoInteractions(callerGuard, userRepository);
    }

    private void stubPredicates() {
        when(root.get("municipality")).thenReturn(municipalityPath);
        when(municipalityPath.get("id")).thenReturn(municipalityIdPath);
        when(cb.equal(municipalityIdPath, 10L)).thenReturn(municipalityPredicate);
        when(root.get("role")).thenReturn(rolePath);
        when(cb.equal(rolePath, AppUserRole.FIELD_WORKER)).thenReturn(rolePredicate);
        when(root.get("team")).thenReturn(teamPath);
        when(teamPath.get("id")).thenReturn(teamIdPath);
        when(cb.equal(teamIdPath, 30L)).thenReturn(teamPredicate);
        when(root.get("status")).thenReturn(statusPath);
        when(cb.equal(statusPath, AppUserStatus.ACTIVE)).thenReturn(statusPredicate);
        when(root.<String>get("name")).thenReturn(namePath);
        when(cb.lower(namePath)).thenReturn(lowerName);
        when(cb.like(lowerName, "%ana%")).thenReturn(namePredicate);
        when(cb.and(municipalityPredicate, rolePredicate, teamPredicate, statusPredicate, namePredicate))
                .thenReturn(combinedPredicate);
    }
}
