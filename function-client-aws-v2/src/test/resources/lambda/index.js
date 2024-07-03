exports.handler = async (event, context) => {
    if (!event.aNumber || !event.aString || !event.aObject || !event.aObject.aNumber || !event.aObject.aString) {
      throw new Error('Invalid Input');
    }

    const arr = [];
    arr.push(event.aObject);

    const response = {
        aNumber: event.aNumber,
        aString: event.aString,
        aObject: event.aObject,
        anArray: arr,
    };

    return response
};
