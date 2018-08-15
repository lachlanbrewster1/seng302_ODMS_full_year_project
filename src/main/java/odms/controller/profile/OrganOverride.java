package odms.controller.profile;

import odms.controller.CommonController;
import odms.controller.database.DAOFactory;
import odms.model.profile.Profile;
import odms.model.user.User;

public class OrganOverride extends CommonController {
    odms.view.profile.OrganOverride view;

    public OrganOverride(odms.view.profile.OrganOverride view) {
        this.view = view;
    }

    public void confirm() {
        System.out.println("HERE");
        Integer isExpired = 1;
        DAOFactory.getOrganDao().setExpired(view.getCurrentProfile(), view.getCurrentOrgan(), isExpired,
                view.getReasonText(), view.getCurrentUser().getStaffID());
    }
}
