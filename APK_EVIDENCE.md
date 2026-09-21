# APK Evidence — V18

Verified from Chat Mig33 1.10 APK static resources and DEX strings:

- package: com.newnimbuzz
- version: 1.10
- custom activities: 33
- original layouts include: home, main, praivet, praivet0, chat5as, chat_id, profaile, users, users_id, follow, folo2, folo5, coments, setting, settings, mtjar, addmarchint, mirchint0, marchnt_balance, ban, banlist, newgrop, nworld, sirya, etc.
- Firebase Auth, Realtime Database, Storage and Messaging classes are present.
- original strings explicitly include Login, register now, Private talk, Create a chat room, Room settings, Notifications, Block list, Transfer points, Send points, Raise the chat room, Profile settings, English language.
- original Firebase-related node names/keys observed include users, chat, chatroom, follow, blocking, bana/banlist, balance, merchant/marchant, comment/coments, notification.
- emoji.db is packaged in assets and the APK contains a large emoji image set.
- no ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission was observed.

V18 changes the reconstruction flow so MainActivity represents authentication while HomeActivity represents the post-login application shell, matching the original activity naming evidence more closely.
