name: Feature Request
about: Suggest a new idea or improvement for Privacy Lock.
title: "[FEATURE] "
labels: enhancement
assignees: ''

body:
  - type: markdown
    attributes:
      value: |
        Thank you for suggesting a feature! Please check existing issues first to make sure your feature hasn't been proposed before.

  - type: textarea
    id: problem
    attributes:
      label: Is your feature request related to a problem?
      description: A clear and concise description of what the problem is.
      placeholder: "e.g., I'm always frustrated when..."
    validations:
      required: false

  - type: textarea
    id: solution
    attributes:
      label: Describe the Solution You'd Like
      description: A clear and concise description of what you want to happen.
    validations:
      required: true

  - type: textarea
    id: alternatives
    attributes:
      label: Describe Alternatives You've Considered
      description: A clear and concise description of any alternative solutions or features you've considered.
    validations:
      required: false

  - type: textarea
    id: security_impact
    attributes:
      label: Security & Privacy Considerations
      description: How does this feature impact user security, encryption, or local storage?
      placeholder: "e.g., This requires persisting a new category value in the Room database, which will be stored locally..."
    validations:
      required: true

  - type: textarea
    id: additional_context
    attributes:
      label: Additional Context
      description: Add any other context, wireframes, screenshots, or Material 3 mockups here.
