# sdl_core_ci
Repository for housing continuous integration related scripts and files for SDL Core.

https://opensdl-jenkins.prjdmz.luxoft.com/

Currently, we are using Jenkins server for the Continuous Integration process of our project.
It is mainly used for the following functionality: 

    - building SDL_CORE / SDL_ATF
    - running ATF test scripts to check implemented functionality
    - checking coding style and cppcheck

Here is a matrix of build jobs:

| Job's name mask | Description |
| --- | --- |
| _P / _E / _H | build on Proprietary / External Proprietary / HTTP flows |
| BWSS_OFF | builds with disabled option: -DBUILD_WEBSOCKET_SERVER_SUPPORT=OFF |
| _noUT / _UT | build without UnitTests / with UnitTest |
| _EL_OFF | build with disabled logging: -DENABLE_LOG=OFF |
| _ES_OFF | build with disabled security: -DENABLE_SECURITY=OFF |
| _UC_OFF | build with UnitTests with disabled cotire option: -DUSE_COTIRE=OFF |
| _BCAF | builds with disabled option: -DBUILD_CLOUD_APP_SUPPORT=OFF |
| _ATF_BUILD | a job which builds ATF for reuse in all the ATF test jobs |
| _TCP | ATF scripts jobs with TCP transport type |
| _WS | ATF scripts jobs with WebSocket transport type |
| _WSS | ATF scripts jobs with WebSocketSecured transport type |
| _OFF |  jobs view that are running on sdl_core built in *_BWSS_OFF jobs previously described |

Main view builds shows ATF build, UnitTests coverage, and a lot of sdl_core build jobs with different flags.

There are several regression views that run all our ATF tests from develop on each Policy flow (Proprietary, External Proprietary, HTTP) and transport type (TCP, WebSocket, WebSocketSecured), on TCP using Remote ATF, and a view with ATF test jobs which use sdl_core built without WebSocket support. 

There are three main directories:

- `dockerfiles` contains Dockerfiles for building docker images for Jenkins cloud resources (build atf, build sdl_core, runt atf test jobs)
- `jenkins_configs` contains Jenkins xml config files for two main job types - build sdl_core and run smoke test atf test job.
- `scripts` contains bash scripts wich are used in jobs

# FAQ

# Is there a way for me to verify the jenkins server is pulling the scripts from github?
Yes, we just should open `Console Output` log in Jenkins UI for needed job and find there string with downloading file from the `raw.githubusercontent.com` .

# How an SDLC member would be able to integrate the new CI scripts into a jenkins server.
Mainly, this is the way: create jobs from xml configs, create cloud resources from Dockerfiles, use bash scripts.
