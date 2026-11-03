package uz.devops.bm.service.errors;

public class NameAlreadyUsedException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public NameAlreadyUsedException() {
        super(ErrorConstants.NAME_ALREADY_USED_TYPE, "Name is already in use!", "userManagement", "emailexists");
    }
}
