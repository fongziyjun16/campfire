import {createSlice} from "@reduxjs/toolkit";

export const avatarUrlSlice = createSlice({
    name: "avatarUrl",
    initialState: {
        value: 0
    },
    reducers: {
        updateAvatarUrl: (state, action) => void(state.value = action.payload)
    }
})

export const {updateAvatarUrl} = avatarUrlSlice.actions;

export const selectAvatarUrl = (state) => state.avatarUrl.value

export default avatarUrlSlice.reducer