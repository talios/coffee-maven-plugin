class Animal
  constructor: (@name) ->

  move: (meters) ->
    print @name + " moved #{meters}m."
