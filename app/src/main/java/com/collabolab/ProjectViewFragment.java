package com.collabolab;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.collabolab.Fragments.CustomFragment;
import com.collabolab.Fragments.CustomProjectFragment;
import com.collabolab.Utilities.FirebaseTools;
import com.collabolab.model.PermissionSet;
import com.collabolab.model.Project;
import com.collabolab.model.ProjectMember;
import com.collabolab.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;


public class ProjectViewFragment extends CustomProjectFragment {

    TextView ownerTV,descTV;
    ImageView chatBtn;
    TextView tasksBtn;
    User owner;
    ArrayList<ProjectMember> members;
    LinearLayout membersContainer;
    LinearLayout inviteCollaboratorBtn;
    ProjectMember selectedMember;
    public ProjectViewFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        descTV = container.findViewById(R.id.projectDescription);
        ownerTV = container.findViewById(R.id.ownerTV);
        chatBtn = container.findViewById(R.id.chatBtn);
        tasksBtn = container.findViewById(R.id.viewTasksBtn);
        inviteCollaboratorBtn = container.findViewById(R.id.inviteCollaboratorBtn);
        inviteCollaboratorBtn.setOnClickListener(view1 -> {
            if (!PermissionSet.userPermissions.hasPermission(PermissionSet.PERMISSION_INVITE)){
                Toast.makeText(getContext(),"You do not have permission to invite collaborators",Toast.LENGTH_SHORT).show();
            }
            else{
                parent.setFragment(new InviteFragment());
            }
        });
        descTV.setText(currentProject.description);
        membersContainer = container.findViewById(R.id.membersContainer);
        chatBtn.setOnClickListener(view1 -> {
            parent.setFragment(new ChatFragment());
        });
        tasksBtn.setOnClickListener(view1 -> {
            parent.setFragment(new TaskFragment());
        });
        owner= currentProject.owner;
        if(owner.isLoaded())
            ownerTV.setText(owner.name);
        else
            owner.load((data, success) -> {
                if(success)
                    ownerTV.setText(owner.name);
            });
        ownerTV.setOnClickListener(view1 -> {
            parent.setFragment(new ViewProfileFragment(owner));
        });
        members = currentProject.members;
        if(members!=null){
            for(ProjectMember member:members){
                User user = member.getUserInfo();
                if(user.isLoaded())
                    addMemberCard(user);
                else
                    member.getUserInfo().load((data, success) -> {
                        if(success){
                            addMemberCard(user);
                        }
                    });
            }
        }
    }

    @Override
    public ImageButton getActiveButton() {
        return SharedLayout.parentActivity.projectsBtn;
    }

    @Override
    public String getTitle() {
        if (currentProject!=null)
            return currentProject.getName();
        return Project.currentProject.getName();
    }

    private void addMemberCard(User user){
        View memberCard = getLayoutInflater().inflate(R.layout.member_card,null);
        //add margin to the card
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(80,15,80,15);
        memberCard.setLayoutParams(params);
        membersContainer.addView(memberCard);
        TextView nameTV = memberCard.findViewById(R.id.memberNameTV);
        nameTV.setText(user.name);
        ImageView menuBtn = memberCard.findViewById(R.id.memberMenuBtn);
        registerForContextMenu(menuBtn);
        menuBtn.setOnClickListener(view -> {
            view.showContextMenu();
        });
        int index = -1;
        for(int i=0;i<members.size();i++){
            if(members.get(i).getUserInfo().getId().equals(user.getId())){
                index = i;
                break;
            }
        }
        memberCard.findViewById(R.id.memberMenuBtn).setTag(index);

    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = parent.getMenuInflater();
        inflater.inflate(R.menu.project_member_menu,menu);
        int index = (int) v.getTag();
        selectedMember = members.get(index);
        PermissionSet otherUserPermissions = selectedMember.getPermissions();
        modifyMenuItems(menu,otherUserPermissions);
    }



    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.permissionCreateTaskMI:
                selectedMember.getPermissions().togglePermission(PermissionSet.PERMISSION_CREATE_TASK);
                break;
            case R.id.permissionDeleteTaskMI:
                selectedMember.getPermissions().togglePermission(PermissionSet.PERMISSION_DELETE_TASK);
                break;
            case R.id.permissionEditTaskMI:
                selectedMember.getPermissions().togglePermission(PermissionSet.PERMISSION_EDIT_TASK);
                break;
            case R.id.permissionReviewTaskMI:
                selectedMember.getPermissions().togglePermission(PermissionSet.PERMISSION_REVIEW_TASK);
                break;
            case R.id.permissionKickMI:
                selectedMember.getPermissions().togglePermission(PermissionSet.PERMISSION_KICK_MEMBER);
                break;
            case R.id.permissionInviteMI:
                selectedMember.getPermissions().togglePermission(PermissionSet.PERMISSION_INVITE);
                break;
            case R.id.removeFromProjectMI:
                removeFromProject(selectedMember);
                break;
            case R.id.viewProfileMI:
                parent.setFragment(new ViewProfileFragment(selectedMember.getUserInfo()));
                break;
            case R.id.permissionManagePermissionsMI:
                selectedMember.getPermissions().togglePermission(PermissionSet.PERMISSION_MANAGE_PERMISSIONS);
                break;

        }
        selectedMember.getPermissions().save();
        return super.onContextItemSelected(item);
    }

    private void modifyMenuItems(ContextMenu menu, PermissionSet otherUserPermissions){
        MenuItem permissionManagePermissionsMI = menu.findItem(R.id.permissionManagePermissionsMI);
        permissionManagePermissionsMI.setVisible(true);
        MenuItem managePermissionsMI =  menu.findItem(R.id.managePermissionsMI);
        managePermissionsMI.setVisible(false);
        MenuItem permissionCreateTaskMI = menu.findItem(R.id.permissionCreateTaskMI);
        permissionCreateTaskMI.setVisible(false);
        MenuItem permissionDeleteTaskMI = menu.findItem(R.id.permissionDeleteTaskMI);
        permissionDeleteTaskMI.setVisible(false);
        MenuItem permissionEditTaskMI = menu.findItem(R.id.permissionEditTaskMI);
        permissionEditTaskMI.setVisible(false);
        MenuItem permissionReviewTaskMI = menu.findItem(R.id.permissionReviewTaskMI);
        permissionReviewTaskMI.setVisible(false);
        MenuItem permissionKickMI = menu.findItem(R.id.permissionKickMI);
        permissionKickMI.setVisible(false);
        MenuItem permissionInviteMI = menu.findItem(R.id.permissionInviteMI);
        permissionInviteMI.setVisible(false);
        MenuItem removeFromProjectMI = menu.findItem(R.id.removeFromProjectMI);
        removeFromProjectMI.setVisible(false);
        PermissionSet userPermissions = PermissionSet.userPermissions;
        if(userPermissions!=null){
            if(userPermissions.hasPermission(PermissionSet.PERMISSION_MANAGE_PERMISSIONS)){
                managePermissionsMI.setVisible(true);
                if (userPermissions.hasPermission(PermissionSet.PERMISSION_CREATE_TASK)){
                    permissionCreateTaskMI.setVisible(true);
                    boolean otherhasPermission = otherUserPermissions.hasPermission(PermissionSet.PERMISSION_CREATE_TASK);
                    permissionCreateTaskMI.setTitle(otherhasPermission?"Revoke Create Task Permission":"Grant Create Task Permission");
                }
                if (userPermissions.hasPermission(PermissionSet.PERMISSION_DELETE_TASK)){
                    permissionDeleteTaskMI.setVisible(true);
                    boolean otherhasPermission = otherUserPermissions.hasPermission(PermissionSet.PERMISSION_DELETE_TASK);
                    permissionDeleteTaskMI.setTitle(otherhasPermission?"Revoke Delete Task Permission":"Grant Delete Task Permission");
                }
                if (userPermissions.hasPermission(PermissionSet.PERMISSION_EDIT_TASK)){
                    permissionEditTaskMI.setVisible(true);
                    boolean otherhasPermission = otherUserPermissions.hasPermission(PermissionSet.PERMISSION_EDIT_TASK);
                    permissionEditTaskMI.setTitle(otherhasPermission?"Revoke Edit Task Permission":"Grant Edit Task Permission");
                }
                if (userPermissions.hasPermission(PermissionSet.PERMISSION_REVIEW_TASK)){
                    permissionReviewTaskMI.setVisible(true);
                    boolean otherhasPermission = otherUserPermissions.hasPermission(PermissionSet.PERMISSION_REVIEW_TASK);
                    permissionReviewTaskMI.setTitle(otherhasPermission?"Revoke Review Task Permission":"Grant Review Task Permission");
                }
                if (userPermissions.hasPermission(PermissionSet.PERMISSION_KICK_MEMBER)){
                    permissionKickMI.setVisible(true);
                    boolean otherhasPermission = otherUserPermissions.hasPermission(PermissionSet.PERMISSION_KICK_MEMBER);
                    permissionKickMI.setTitle(otherhasPermission?"Revoke Kick Member Permission":"Grant Kick Member Permission");

                }
                if (userPermissions.hasPermission(PermissionSet.PERMISSION_INVITE)){
                    permissionInviteMI.setVisible(true);
                    boolean otherhasPermission = otherUserPermissions.hasPermission(PermissionSet.PERMISSION_INVITE);
                    permissionInviteMI.setTitle(otherhasPermission?"Revoke Invite Permission":"Grant Invite Permission");
                }
                if (owner.getId().equals(User.currentUser.getId())){
                    permissionManagePermissionsMI.setVisible(true);
                    boolean otherhasPermission = otherUserPermissions.hasPermission(PermissionSet.PERMISSION_MANAGE_PERMISSIONS);
                    permissionManagePermissionsMI.setTitle(otherhasPermission?"Revoke Manage Permissions Permission":"Grant Manage Permissions Permission");
                }
            }

        }
        if(userPermissions.hasPermission(PermissionSet.PERMISSION_KICK_MEMBER)){
            removeFromProjectMI.setVisible(true);
        }
    }

    private void removeFromProject(ProjectMember member){
        HashMap<String, Object> data = new HashMap<>();
        data.put("projectId", currentProject.getDocRef().getId());
        data.put("userId", member.getUserInfo().getDocRef().getId());
        data.put("userDecision", false);
        FirebaseTools.functions.getHttpsCallable("removeFromProject").call(data);
        for (int i=0;i<members.size();i++){
            if(members.get(i).getUserInfo().getId().equals(member.getUserInfo().getId())){
                members.remove(i);
                currentProject.members.remove(i);
                break;
            }
        }
        membersContainer.removeAllViews();
        for(ProjectMember m:members){
            User user = m.getUserInfo();
            addMemberCard(user);
        }

    }
}