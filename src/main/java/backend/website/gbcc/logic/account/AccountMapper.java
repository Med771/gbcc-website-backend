package backend.website.gbcc.logic.account;

import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "name", source = "requestDto.name")
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "email", source = "requestDto.email")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "isPasswordSet", constant = "true")
    @Mapping(target = "role", expression = "java(AccountRole.ADMIN)")
    @Mapping(target = "registrationStatus", expression = "java(AccountRegistrationStatus.ACTIVE)")
    @Mapping(target = "isBlocked", constant = "false")
    AccountEntity toAdminEntity(CreateAdminAccountRequestDto requestDto, String passwordHash);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "isPasswordSet", constant = "true")
    @Mapping(target = "role", expression = "java(AccountRole.OWNER)")
    @Mapping(target = "registrationStatus", expression = "java(AccountRegistrationStatus.ACTIVE)")
    @Mapping(target = "isBlocked", constant = "false")
    AccountEntity toOwnerEntity(String email, String passwordHash);

    AccountResponseDto toResponse(AccountEntity entity);
}
