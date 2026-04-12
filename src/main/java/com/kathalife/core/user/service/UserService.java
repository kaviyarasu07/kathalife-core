package com.kathalife.core.user.service;

import com.kathalife.core.user.dto.*;
import com.kathalife.core.user.entity.User;

public interface UserService {
    UserResponse getCurrentUser(User currentUser);
    BioProfileResponse getBioProfile(User currentUser);
    BioProfileResponse updateBioProfile(User currentUser, BioProfileRequest request);
    LifeSummaryResponse getLifeSummary(User currentUser);
    LifeSummaryResponse updateLifeSummary(User currentUser, LifeSummaryRequest request);
}
