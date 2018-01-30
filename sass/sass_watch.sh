#!/bin/bash

sass --style compressed --sourcemap=none --watch web/style.sass:../src/main/resources/web/static/css/style.min.css javafx/style.sass:../src/main/resources/javafx/css/main.min.css
