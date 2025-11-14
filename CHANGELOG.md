# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Automated CI/CD pipeline with GitHub Actions
- Release workflow for automated APK/AAB building and publishing
- ProGuard rules for optimized release builds
- Comprehensive unit tests for MainActivity
- Enhanced error handling in MainActivity
- Internet and network state permissions in manifest
- Pull request template for better contribution workflow
- Detailed README with build and release instructions

### Changed
- Enhanced MainActivity with better error handling and logging
- Improved Gradle configuration with signing support
- Updated dependencies with testing libraries
- Enhanced CI workflow with lint checks and artifact uploads
- Improved string resources with better error messages

### Fixed
- Missing ProGuard configuration file
- Potential crashes when no browser is available
- Missing internet permissions in manifest

## [1.0.0] - Initial Release

### Added
- Initial Android application setup
- Material Design 3 UI
- Coil image loading integration
- Basic CI workflow
- ViewBinding support