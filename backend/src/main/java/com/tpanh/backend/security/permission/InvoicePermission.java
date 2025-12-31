package com.tpanh.backend.security.permission;

import com.tpanh.backend.repository.BuildingRepository;
import com.tpanh.backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("invoicePermission")
@RequiredArgsConstructor
public class InvoicePermission extends AbstractPermission {

    private final InvoiceRepository invoiceRepository;
    private final BuildingRepository buildingRepository;

    public boolean canAccessInvoice(final Integer invoiceId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return invoiceRepository.existsByIdAndRoomBuildingManagerId(
                invoiceId, principal.getUserId());
    }

    public boolean canAccessBuildingInvoices(
            final Integer buildingId, final Authentication authentication) {
        final var principal = extractPrincipal(authentication);
        if (principal == null) {
            return false;
        }

        if (principal.hasRole("ADMIN")) {
            return true;
        }

        return buildingRepository.existsByIdAndManagerId(buildingId, principal.getUserId());
    }
}
