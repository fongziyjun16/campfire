import {createSlice} from "@reduxjs/toolkit";

export const usernameSlice = createSlice({
    name: "username",
    initialState: {
        value: ""
    },
    reducers: {
        updateUsername: (state, action) => void(state.value = action.payload)
    }
})

export const {updateUsername} = usernameSlice.actions;

export const selectUsername = state => state.username.value

export default usernameSlice.reducer