package com.knowledgeassistant.usermanagementservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.knowledgeassistant.usermanagementservice.client.AuthServiceClient;
import com.knowledgeassistant.usermanagementservice.customexception.BadRequestException;
import com.knowledgeassistant.usermanagementservice.dto.UpdateStatusRequestDto;
import com.knowledgeassistant.usermanagementservice.dto.UserResponseDto;
import com.knowledgeassistant.usermanagementservice.enums.AccountStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
//@Transactional  not required as it doesnt write in ums_db
public class UserService {

	// Not required because of feign client
//	private final UserRepository userRepo;
	
	private final AuthServiceClient authClient;
	
	public UserResponseDto getUser(Long userId) {
		
		
		return authClient.getUserById(userId);
	}
	
	 
	   public List<UserResponseDto> getAllUsers() {
	        return authClient.getAllUsers();
	    }
	   
	   
	   
	   public String approveUser(Long userId,Long approverId) {
		   UserResponseDto user = authClient.getUserById(userId);
		   if(user.getAccountStatus()==AccountStatus.ACTIVE) {
			   throw new BadRequestException("User " + user.getEmail() + " is already approved");
		   }
		   if(user.getAccountStatus()==AccountStatus.REJECTED) {
			   throw new BadRequestException("User " + user.getEmail() + " is rejected and cannot be approved");
		   }
		   UserResponseDto approver = authClient.getUserById(approverId);
		   
		   boolean isApproverAdmin = approver.getRoles().contains("ROLE_ADMIN");
		   boolean isApproverManager = approver.getRoles().contains("ROLE_MANAGER");
		   boolean targetIsManager = user.getRoles().contains("ROLE_MANAGER");
		   boolean targetIsEmployee = user.getRoles().contains("ROLE_EMPLOYEE");
	        
	        if(targetIsManager && !isApproverAdmin) {
	        	throw new BadRequestException("Only an ADMIN can approve a MANAGER account!");
	        }
	        
	        if (targetIsEmployee && !isApproverManager && !isApproverAdmin) {
	            throw new BadRequestException("Only a MANAGER or ADMIN can approve an EMPLOYEE account!");
	        }
//	        user.setAccountStatus(AccountStatus.ACTIVE);
//	        user.setApprovedBy(approver.getEmail());
//	        user.setApprovedAt(LocalDate.now());
	        authClient.updateUserStatus(userId, new UpdateStatusRequestDto(AccountStatus.ACTIVE, approver.getEmail()));
		    return "User "+user.getEmail()+" has been approved successfully..";
	   }
	   
	   public String rejectUser(Long userId,Long approverId){
	    	
	    	UserResponseDto user = authClient.getUserById(userId);
	    	
	    	if (user.getAccountStatus() == AccountStatus.REJECTED) {
	            throw new BadRequestException( "User " + user.getEmail() + " is already rejected.");
	        }
	        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
	            throw new BadRequestException("User " + user.getEmail() + " is already approved/active and cannot be rejected.");
	        }
	    	
	    	UserResponseDto approverUser = authClient.getUserById(approverId);
	    	
	    	boolean isApproverAdmin = approverUser.getRoles().contains("ROLE_ADMIN");
	    	boolean isApproverManager = approverUser.getRoles().contains("ROLE_MANAGER");
	    	boolean targetIsManager = user.getRoles().contains("ROLE_MANAGER");
	    	boolean targetIsEmployee = user.getRoles().contains("ROLE_EMPLOYEE");
	        
	        if(targetIsManager && !isApproverAdmin) {
	        	throw new BadRequestException("Only an ADMIN can reject a MANAGER account!");
	        }
	        
	        if(targetIsEmployee && !isApproverAdmin && !isApproverManager) {
	        	throw new BadRequestException("Only an ADMIN or MANAGER can reject an EMPLOYEE account!");
	        }
	        
//	        user.setAccountStatus(AccountStatus.REJECTED);
	        authClient.updateUserStatus(userId, new UpdateStatusRequestDto(AccountStatus.REJECTED, approverUser.getEmail()));
	        
	    	return "User "+ user.getEmail()+" has been rejected..";
	    }
	   
	    public List<UserResponseDto> getPendingUser(Long callerId) {
	    	UserResponseDto user =authClient.getUserById(callerId);
	    	
	    	boolean isAdmin = user.getRoles().contains("ROLE_ADMIN");
	    	boolean isManager = user.getRoles().contains("ROLE_MANAGER");
	    	
	    	List<UserResponseDto> pendingUsers = authClient.getPendingUsers();
	    	
	    	if(isAdmin) {
	    		return pendingUsers;
	    	}
	    	else if(isManager) {
	    		return pendingUsers.stream().filter(u->u.getRoles().contains("ROLE_EMPLOYEE"))
	    				.collect(Collectors.toList());
	    	}
	    	return List.of();
	    }
	   
	// Not required here because we are doing it with feign client   
//	   private UserResponseDto mapToDto(User user) {
//		   return UserResponseDto.builder()
//				   .id(user.getId())
//				   .username(user.getUserName())
//				   .email(user.getEmail())
//				   .accountStatus(user.getAccountStatus())
//				   .approvedBy(user.getApprovedBy())
//				   .roles(user.getRoles().stream()
//						   .map(Role::getRole)
//						   .collect(Collectors.toSet()))
//				   .build();
//	   }
}
