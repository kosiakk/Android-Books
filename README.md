# Android-Books
Playgroud for Android development - implement Google Books API

## Goal

> Take the Google Books API and produce an Android app that allows a user to scroll through a very long list of books, displaying the cover images and book titles.
> Allow the user to drill into the detail of a given book and in doing so present the full book details.

## Important aspects

`very long list` -> view recycling, pre-fetch, cache
`cover images and book titles` -> minimalistic list view. Google shows annotation and a few more things
`detail of a given book` -> a spearate Activity with complete information

I'm going to implement the app in two steps:
1. Minimal working application with cache, async, and basic Material Design
2. If time permits, work in the UI
