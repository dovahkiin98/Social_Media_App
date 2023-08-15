package net.inferno.socialmedia.ui.main

import android.net.Uri
import net.inferno.socialmedia.model.Comment
import net.inferno.socialmedia.model.Community
import net.inferno.socialmedia.model.Post
import net.inferno.socialmedia.model.User

object Routes {
    const val APPS = "apps"
    const val INTERESTS = "interests"

    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RESET_PASSWORD = "reset_password"

    const val USER_PROFILE = "user/profile"
    const val EDIT_PROFILE = "user/profile/edit"
    const val USER_FOLLOWERS = "user/followers"
    const val USER_FOLLOWINGS = "user/followings"

    const val POST = "post"
    const val ADD_COMMENT = "add_comment"
    const val ADD_POST = "add_post"

    const val NEW_COMMUNITY = "new_community"
    const val COMMUNITY = "community"
    const val COMMUNITY_MEMBERS = "community/members"
    const val COMMUNITY_REQUESTS = "community/requests"
    const val COMMUNITY_PENDING_POSTS = "community/pendingPosts"

    const val CONVERSATIONS = "conversations"

    const val IMAGE = "image"

    fun profile(user: User?): String {
        val uri = Uri.Builder()
            .path(USER_PROFILE)

        if (user != null) {
            uri.appendQueryParameter("userId", user.id)
        }

        return uri.toString()
    }

    fun followers(user: User?): String {
        val uri = Uri.Builder()
            .path(USER_FOLLOWERS)

        if (user != null) {
            uri.appendQueryParameter("userId", user.id)
        }

        return uri.toString()
    }

    fun followings(user: User?): String {
        val uri = Uri.Builder()
            .path(USER_FOLLOWINGS)

        if (user != null) {
            uri.appendQueryParameter("userId", user.id)
        }

        return uri.toString()
    }

    fun post(post: Post): String {
        val uri = Uri.Builder()
            .path(POST)
            .appendQueryParameter("postId", post.id)

        return uri.toString()
    }

    fun image(imageUrl: String) = Uri.Builder()
        .path(IMAGE)
        .appendQueryParameter("imageUrl", imageUrl)
        .toString()

    fun addPost(
        post: Post?,
        communityId: String? = null,
    ) = Uri.Builder()
        .path(ADD_POST)
        .appendQueryParameter("postId", post?.id)
        .appendQueryParameter("communityId", communityId)
        .toString()

    fun addComment(
        post: Post? = null,
        comment: Comment? = null,
    ) = Uri.Builder()
        .path(ADD_COMMENT)
        .appendQueryParameter("postId", post?.id ?: comment?.postId)
        .appendQueryParameter("commentId", comment?.id)
        .toString()

    fun editComment(
        comment: Comment? = null,
    ) = Uri.Builder()
        .path(ADD_COMMENT)
        .appendQueryParameter("postId", null)
        .appendQueryParameter("commentId", comment?.id)
        .toString()


    fun community(
        community: Community,
    ) = Uri.Builder()
        .path(COMMUNITY)
        .appendQueryParameter("communityId", community.id)
        .toString()

    fun communityMembers(
        community: Community,
    ) = Uri.Builder()
        .path(COMMUNITY_MEMBERS)
        .appendQueryParameter("communityId", community.id)
        .toString()

    fun communityRequests(
        community: Community,
    ) = Uri.Builder()
        .path(COMMUNITY_REQUESTS)
        .appendQueryParameter("communityId", community.id)
        .toString()

    fun pendingPosts(
        community: Community,
    ) = Uri.Builder()
        .path(COMMUNITY_PENDING_POSTS)
        .appendQueryParameter("communityId", community.id)
        .toString()

    fun conversation(
        conversationId: String,
    )= Uri.Builder()
        .path(CONVERSATIONS)
        .appendQueryParameter("conversationId", conversationId)
        .toString()
}