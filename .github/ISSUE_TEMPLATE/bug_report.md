name: Bug Report
about: Create a report to help us improve Privacy Lock.
title: "[BUG] "
labels: bug, triage
assignees: ''

body:
  - type: markdown
    attributes:
      value: |
        Thank you for reporting a bug! Before creating a new issue, please search existing issues to see if this has already been reported.
        *If this is a security vulnerability, do NOT report it here. Follow instructions in [SECURITY.md](https://github.com/your-repo/SECURITY.md) instead.*

  - type: textarea
    id: description
    attributes:
      label: Bug Description
      description: A clear and concise description of what the bug is.
      placeholder: "e.g., When launching Settings with Decoy mode active, the app displays..."
    validations:
      required: true

  - type: textarea
    id: steps
    attributes:
      label: Steps to Reproduce
      description: Explain how we can reproduce the behavior.
      placeholder: |
        1. Go to '...'
        2. Click on '....'
        3. Scroll down to '....'
        4. See error
    validations:
      required: true

  - type: textarea
    id: expected
    attributes:
      label: Expected Behavior
      description: What you expected to happen.
    validations:
      required: true

  - type: textarea
    id: environment
    attributes:
      label: Environment Info
      description: Please provide details about your environment.
      placeholder: |
        - Device: [e.g., Google Pixel 7]
        - Android Version: [e.g., Android 14 (API 34)]
        - App Version: [e.g., 1.0.0]
    validations:
      required: true

  - type: textarea
    id: additional_context
    attributes:
      label: Additional Context
      description: Add any other context, screenshots, stack traces, or log snippets here.
