package backend.website.gbcc.logic.account;

import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "firstName", expression = "java(adminFirstName(requestDto))")
    @Mapping(target = "lastName", expression = "java(adminLastName(requestDto))")
    @Mapping(target = "patronymic", ignore = true)
    @Mapping(target = "phone", source = "requestDto.phone")
    @Mapping(target = "email", source = "requestDto.email")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "isPasswordSet", constant = "true")
    @Mapping(target = "role", expression = "java(AccountRole.ADMIN)")
    @Mapping(target = "registrationStatus", expression = "java(AccountRegistrationStatus.ACTIVE)")
    @Mapping(target = "isBlocked", constant = "false")
    @Mapping(target = "referralCode", ignore = true)
    @Mapping(target = "referredBy", ignore = true)
    @Mapping(target = "crmOrganization", ignore = true)
    @Mapping(target = "broughtByManager", ignore = true)
    AccountEntity toAdminEntity(CreateAdminAccountRequestDto requestDto, String passwordHash);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "firstName", constant = "")
    @Mapping(target = "lastName", constant = "")
    @Mapping(target = "patronymic", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "isPasswordSet", constant = "true")
    @Mapping(target = "role", expression = "java(AccountRole.OWNER)")
    @Mapping(target = "registrationStatus", expression = "java(AccountRegistrationStatus.ACTIVE)")
    @Mapping(target = "isBlocked", constant = "false")
    @Mapping(target = "referralCode", ignore = true)
    @Mapping(target = "referredBy", ignore = true)
    @Mapping(target = "crmOrganization", ignore = true)
    @Mapping(target = "broughtByManager", ignore = true)
    AccountEntity toOwnerEntity(String email, String passwordHash);

    @Mapping(target = "crmOrganizationId", source = "crmOrganization.id")
    @Mapping(target = "broughtByManagerId", source = "broughtByManager.id")
    AccountResponseDto toResponse(AccountEntity entity);

    default String adminFirstName(CreateAdminAccountRequestDto requestDto) {
        if (StringUtils.hasText(requestDto.firstName())) {
            return requestDto.firstName().trim();
        }
        return requestDto.name().trim();
    }

    default String adminLastName(CreateAdminAccountRequestDto requestDto) {
        return StringUtils.hasText(requestDto.lastName()) ? requestDto.lastName().trim() : "";
    }
}
