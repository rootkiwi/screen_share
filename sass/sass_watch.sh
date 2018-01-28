#!/bin/bash

sass --style compressed --sourcemap=none --watch web/style.sass:../src/main/resources/public/css/style.min.css javafx/style.sass:../src/main/resources/css/main.min.css


