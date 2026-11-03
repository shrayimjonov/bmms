package uz.paykap.service.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the UserMachineGroup entity.
 */
public class UserMachineGroupDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 50)
    private String username;

    private Set<MachineGroupDTO> machineGroups = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<MachineGroupDTO> getMachineGroups() {
        return machineGroups;
    }

    public void setMachineGroups(Set<MachineGroupDTO> machineGroups) {
        this.machineGroups = machineGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserMachineGroupDTO userMachineGroupDTO = (UserMachineGroupDTO) o;
        if (userMachineGroupDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), userMachineGroupDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "UserMachineGroupDTO{" +
            "id=" + getId() +
            ", username='" + getUsername() + "'" +
            "}";
    }
}
